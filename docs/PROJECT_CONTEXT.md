# Project Context - YtdlpJava

## Current State
The project is a Java-based wrapper around `yt-dlp` for downloading and processing YouTube content (subtitles, audio, video, screenshots). It follows a clean architecture with interfaces and strategy pattern for different tasks.

## Recent Changes (2026-05-08)
- **Improved Subtitle Cleaning Logic**: Refactored `SubtitleCleaner` to produce more natural text.
  - Subtitle lines are now joined into paragraphs between timestamp headers, significantly reducing line breaks.
  - Removed aggressive automatic dot addition at the end of every subtitle line, which was causing "too many dots" in auto-generated subtitles.
  - Improved de-duplication of scrolling VTT lines within paragraphs.
  - Added comprehensive tests for auto-generated and scrolling subtitles.
- **Fixed Subtitle Path Resolution**: (2026-05-05) ...
  - Removed reliance on `yt-dlp --print after_move:filepath` for subtitles as it returns empty with `--skip-download`.
  - Implemented a search mechanism to find the `.vtt` or `.srt` file created by `yt-dlp` based on the requested language and output basename.
- **Added Interactive Language Selection**: The tool now prompts the user for a subtitle language if it's not provided via CLI parameters.
- **Implemented Real-time Progress Tracking**:
  - Refactored `ProcessExecutor` to stream output to the log in real-time.
  - Added `--newline` and `--progress` flags to `yt-dlp` commands in all downloaders to ensure visible feedback during long operations.
- **Organized Storage Structure**:
  - Subtitles are now automatically saved to the `txt/` directory.
  - Audio, video, and screenshots are organized into `output/audio/`, `output/video/`, and `output/img/` respectively.
- **Robust Path & Filename Handling**:
  - Improved `ProcessExecutor` to filter out yt-dlp warnings and accurately extract absolute file paths.
  - Reduced default filename limit to 60 characters to ensure compatibility with OS filename limits during conversion/download.
- **Universal URL Handling**: Verified and improved support for both single videos and playlists.

## Pending Items
- [ ] Add integration tests that use a mock `yt-dlp` or controlled environment.
- [ ] Implement retry logic for failed `yt-dlp` commands.
- [ ] Support for more video metadata extraction (tags, description).
