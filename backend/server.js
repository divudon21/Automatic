require('dotenv').config();
const express = require('express');
const cors = require('cors');
const fetch = require('node-fetch');
const YTMusic = require('ytmusic-api').default;

const app = express();
const PORT = process.env.PORT || 3000;

// ─── Middleware ───────────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());

// ─── YTMusic singleton ────────────────────────────────────────────────────────
let ytmusic = null;
let ytmusicReady = false;

async function initYTMusic() {
  try {
    ytmusic = new YTMusic();
    await ytmusic.initialize();
    ytmusicReady = true;
    console.log('[YTMusic] Initialized successfully');
  } catch (err) {
    console.error('[YTMusic] Init failed:', err.message);
    ytmusicReady = false;
  }
}
initYTMusic();

// ─── Piped instances (fallback chain) ────────────────────────────────────────
const PIPED_INSTANCES = [
  'https://pipedapi.kavin.rocks',
  'https://piped-api.garudalinux.org',
  'https://api.piped.projectsegfau.lt',
  'https://piped.video/api',
];

// ─── Helpers ─────────────────────────────────────────────────────────────────

/**
 * Parse a YTMusic song result into a clean unified object.
 * Handles both standard songs and edge cases (Phonk, International, etc.)
 */
function parseSong(item) {
  if (!item) return null;

  // videoId
  const videoId = item.videoId || item.id || null;
  if (!videoId) return null;

  // title
  const title = item.name || item.title || 'Unknown Title';

  // artist — YTMusic returns artists as array of {name} objects
  let artist = 'Unknown Artist';
  if (Array.isArray(item.artist)) {
    artist = item.artist.map(a => a.name || a).filter(Boolean).join(', ');
  } else if (item.artist && typeof item.artist === 'object' && item.artist.name) {
    artist = item.artist.name;
  } else if (typeof item.artist === 'string') {
    artist = item.artist;
  } else if (Array.isArray(item.artists)) {
    artist = item.artists.map(a => a.name || a).filter(Boolean).join(', ');
  }

  // duration in seconds
  let duration = 0;
  if (typeof item.duration === 'number') {
    duration = item.duration;
  } else if (item.duration && typeof item.duration === 'object') {
    // { label: "3:45", totalSeconds: 225 }
    duration = item.duration.totalSeconds || 0;
  } else if (typeof item.duration === 'string') {
    const parts = item.duration.split(':').map(Number);
    if (parts.length === 2) duration = parts[0] * 60 + parts[1];
    else if (parts.length === 3) duration = parts[0] * 3600 + parts[1] * 60 + parts[2];
  }

  // thumbnail — pick highest resolution
  let thumbnail = '';
  const thumbs = item.thumbnails || item.thumbnail || [];
  if (Array.isArray(thumbs) && thumbs.length > 0) {
    // Sort by width descending, pick largest
    const sorted = [...thumbs].sort((a, b) => (b.width || 0) - (a.width || 0));
    thumbnail = sorted[0].url || '';
  } else if (typeof thumbs === 'string') {
    thumbnail = thumbs;
  }

  // album
  let album = '';
  if (item.album && typeof item.album === 'object') {
    album = item.album.name || '';
  } else if (typeof item.album === 'string') {
    album = item.album;
  }

  return { videoId, title, artist, duration, thumbnail, album };
}

/**
 * Try Piped instances in order to get a streamable audio URL.
 * Returns the best audio-only stream URL (m4a preferred, webm fallback).
 */
async function getStreamUrlFromPiped(videoId) {
  for (const base of PIPED_INSTANCES) {
    try {
      const res = await fetch(`${base}/streams/${videoId}`, {
        headers: { 'User-Agent': 'VibeFlow/1.0' },
        timeout: 8000,
      });
      if (!res.ok) continue;

      const data = await res.json();
      const streams = data.audioStreams || [];

      if (streams.length === 0) continue;

      // Prefer m4a (MPEG_4) at highest bitrate
      const m4aStreams = streams
        .filter(s => s.mimeType && s.mimeType.includes('mp4'))
        .sort((a, b) => (b.bitrate || 0) - (a.bitrate || 0));

      if (m4aStreams.length > 0) {
        return { url: m4aStreams[0].url, mimeType: 'm4a', source: base };
      }

      // Fallback: webm/opus
      const webmStreams = streams
        .filter(s => s.mimeType && s.mimeType.includes('webm'))
        .sort((a, b) => (b.bitrate || 0) - (a.bitrate || 0));

      if (webmStreams.length > 0) {
        return { url: webmStreams[0].url, mimeType: 'webm', source: base };
      }

      // Last resort: first stream
      if (streams[0].url) {
        return { url: streams[0].url, mimeType: 'unknown', source: base };
      }
    } catch (err) {
      console.warn(`[Piped] ${base} failed:`, err.message);
    }
  }
  return null;
}

// ─── Routes ───────────────────────────────────────────────────────────────────

// Health check
app.get('/', (req, res) => {
  res.json({ status: 'ok', service: 'VibeFlow API', ytmusicReady });
});

/**
 * GET /api/search?q=query&limit=20
 * Search YouTube Music for songs.
 */
app.get('/api/search', async (req, res) => {
  const query = (req.query.q || '').trim();
  const limit = Math.min(parseInt(req.query.limit) || 20, 50);

  if (!query) {
    return res.status(400).json({ success: false, error: 'Missing query parameter "q"' });
  }

  // Re-init if not ready
  if (!ytmusicReady) {
    await initYTMusic();
    if (!ytmusicReady) {
      return res.status(503).json({ success: false, error: 'YTMusic not initialized' });
    }
  }

  try {
    // Search specifically for songs (filter: SONG)
    const raw = await ytmusic.searchSongs(query);

    if (!raw || raw.length === 0) {
      return res.json({ success: true, results: [] });
    }

    const results = raw
      .slice(0, limit)
      .map(parseSong)
      .filter(Boolean); // remove nulls (items without videoId)

    return res.json({ success: true, total: results.length, results });
  } catch (err) {
    console.error('[Search] Error:', err.message);
    // Re-init on error
    ytmusicReady = false;
    initYTMusic();
    return res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/trending?genre=bollywood|phonk|international|pop
 * Returns trending/popular songs for a genre.
 */
app.get('/api/trending', async (req, res) => {
  const genre = (req.query.genre || 'bollywood').toLowerCase();

  const queryMap = {
    bollywood: 'top bollywood hits 2024',
    phonk: 'phonk music 2024 trending',
    international: 'top international pop hits 2024',
    pop: 'pop hits 2024',
    hiphop: 'hip hop hits 2024',
    lofi: 'lofi chill beats 2024',
  };

  const searchQuery = queryMap[genre] || `top ${genre} songs 2024`;

  if (!ytmusicReady) {
    await initYTMusic();
    if (!ytmusicReady) {
      return res.status(503).json({ success: false, error: 'YTMusic not initialized' });
    }
  }

  try {
    const raw = await ytmusic.searchSongs(searchQuery);
    const results = (raw || [])
      .slice(0, 20)
      .map(parseSong)
      .filter(Boolean);
    return res.json({ success: true, genre, results });
  } catch (err) {
    console.error('[Trending] Error:', err.message);
    return res.status(500).json({ success: false, error: err.message });
  }
});

/**
 * GET /api/stream/:videoId
 * Returns a direct audio stream URL for ExoPlayer.
 * Tries Piped instances in order.
 */
app.get('/api/stream/:videoId', async (req, res) => {
  const { videoId } = req.params;

  if (!videoId || videoId.length < 5) {
    return res.status(400).json({ success: false, error: 'Invalid videoId' });
  }

  try {
    const result = await getStreamUrlFromPiped(videoId);

    if (!result) {
      return res.status(404).json({
        success: false,
        error: 'Could not resolve stream URL from any Piped instance',
      });
    }

    return res.json({
      success: true,
      videoId,
      streamUrl: result.url,
      mimeType: result.mimeType,
      source: result.source,
    });
  } catch (err) {
    console.error('[Stream] Error:', err.message);
    return res.status(500).json({ success: false, error: err.message });
  }
});

// ─── Start ────────────────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`[VibeFlow API] Running on http://localhost:${PORT}`);
});

module.exports = app;
