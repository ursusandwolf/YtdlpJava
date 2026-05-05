# Project Context - YtdlpJava

## Current State
The project is a Java-based wrapper around `yt-dlp` for downloading and processing YouTube content (subtitles, audio, video, screenshots). It follows a clean architecture with interfaces and strategy pattern for different tasks.

## Recent Changes (2026-05-05)
- **Fixed yt-dlp URL Extraction**: Resolved an issue where `yt-dlp --flat-playlist --print url` returned "NA" for certain videos.
  - Switched from `url` to `webpage_url` in `AbstractYoutubeService.getPlaylistUrls`.
  - Added filtering logic to skip "NA" and empty strings.
  - Implemented a fallback to the original URL if no valid playlist items are extracted.
- **Fixed Subtitle Path Resolution**: Resolved an issue where `SubtitleDownloader` failed to locate downloaded subtitle files.
  - Removed reliance on `yt-dlp --print after_move:filepath` for subtitles as it returns empty with `--skip-download`.
  - Implemented a search mechanism to find the `.vtt` or `.srt` file created by `yt-dlp` based on the requested language and output basename.
- **Added Interactive Language Selection**: The tool now prompts the user for a subtitle language if it's not provided via CLI parameters.
- **Implemented Real-time Progress Tracking**:
  - Refactored `ProcessExecutor` to stream output to the log in real-time.
  - Added `--newline` and `--progress` flags to `yt-dlp` commands in all downloaders to ensure visible feedback during long operations.
- **Universal URL Handling**: Verified and improved support for both single videos and playlists.

## Pending Items
- [ ] Add integration tests that use a mock `yt-dlp` or controlled environment.
- [ ] Implement retry logic for failed `yt-dlp` commands.
- [ ] Support for more video metadata extraction (tags, description).
