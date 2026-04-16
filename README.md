# VibeFlow 🎵

A feature-rich Android music streaming app built with Kotlin + Jetpack Compose.

## Features
- 🎵 **JioSaavn** — Stream Hindi/Bollywood songs (12kbps / 96kbps / 320kbps)
- 📻 **YouTube Music** — Stream via InnerTube API + Piped fallback
- 🔔 **Background Playback** — Spotify-like media notification with controls
- 💾 **Download Songs** — Save songs with quality selection
- 🔄 **Auto-Update** — Checks GitHub Releases for new versions
- 🌍 **Phonk / International / Bollywood** sections

## Download
👉 **[Latest Release](https://github.com/divudon21/Automatic/releases/latest)**

## Build
```bash
./gradlew assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD
Every push to `main` automatically:
1. Builds the APK via GitHub Actions
2. Creates a GitHub Release
3. Uploads the APK as a release asset

The app checks for updates on launch and shows a download dialog if a newer version is available.

## Tech Stack
- Kotlin + Jetpack Compose + Material 3
- ExoPlayer Media3 (background playback)
- NewPipeExtractor (YouTube Music search)
- YouTube InnerTube API (stream resolution)
- JioSaavn API via `saavn.sumit.co`
- Retrofit + OkHttp
- DataStore Preferences
