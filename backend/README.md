# VibeFlow Backend API

Node.js Express server powering YouTube Music search & streaming for VibeFlow.

## Endpoints

### `GET /api/search?q=query&limit=20`
Search YouTube Music for songs.
```json
{
  "success": true,
  "total": 20,
  "results": [
    {
      "videoId": "dQw4w9WgXcQ",
      "title": "Song Name",
      "artist": "Artist Name",
      "duration": 212,
      "thumbnail": "https://i.ytimg.com/vi/.../maxresdefault.jpg",
      "album": "Album Name"
    }
  ]
}
```

### `GET /api/trending?genre=bollywood|phonk|international|pop|hiphop|lofi`
Get trending songs by genre.

### `GET /api/stream/:videoId`
Get direct audio stream URL for ExoPlayer.
```json
{
  "success": true,
  "videoId": "dQw4w9WgXcQ",
  "streamUrl": "https://...",
  "mimeType": "m4a"
}
```

## Deploy

### Vercel
```bash
npm i -g vercel
vercel --prod
```

### VPS
```bash
npm install
node server.js
# or with PM2:
pm2 start server.js --name vibeflow-api
```

## Local Dev
```bash
npm install
npm run dev
```
