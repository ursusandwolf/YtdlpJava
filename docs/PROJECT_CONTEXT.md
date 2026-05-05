# Project Context - YtdlpJava

## Current State
The project is a Java-based wrapper around `yt-dlp` for downloading and processing YouTube content (subtitles, audio, video, screenshots). It follows a clean architecture with interfaces and strategy pattern for different tasks.

## Recent Changes (2026-05-05)
- **Fixed yt-dlp URL Extraction**: Resolved an issue where `yt-dlp --flat-playlist --print url` returned "NA" for certain videos.
  - Switched from `url` to `webpage_url` in `AbstractYoutubeService.getPlaylistUrls`.
  - Added filtering logic to skip "NA" and empty strings.
  - Implemented a fallback to the original URL if no valid playlist items are extracted.
- **Improved Robustness**: The application now handles cases where `yt-dlp` fails to extract individual item URLs in flat-playlist mode by processing the original URL directly.

## Pending Items
- [ ] Add integration tests that use a mock `yt-dlp` or controlled environment.
- [ ] Implement retry logic for failed `yt-dlp` commands.
- [ ] Support for more video metadata extraction (tags, description).
