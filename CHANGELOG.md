# Changelog

All notable changes to this project will be documented in this file.

## [1.3.2] - 2026-05-08

### Fixed
- **Subtitle Cleaning**: Significant improvements to text quality and readability.
  - Added **keyword extraction and highlighting**: top words are automatically identified and bolded.
  - Added **Keywords Summary** section at the end of the file.
  - Switched output to **Markdown (.md)** format with headers for timestamps and **automatic line wrapping** (95 chars).
  - Added **smart filler word removal** (e.g., "э-э", "ну", "так скажем") with Cyrillic support.
  - Implemented **smart capitalization** for sentence continuations across timestamps.
  - Subtitle lines are now joined into paragraphs between timestamps.
  - Removed aggressive automatic dot addition at the end of every line.
  - Improved de-duplication of scrolling lines in VTT files.
  - Added comprehensive tests for all cleaning features.

## [1.3.1] - 2026-05-05

### Fixed
- **yt-dlp URL Extraction**: Fixed a bug where `getPlaylistUrls` would return "NA" for some videos when using `--flat-playlist`. Switched to `webpage_url` and added filtering for invalid results.
- **Subtitle Path Resolution**: Resolved "Invalid argument" error when downloading subtitles. `SubtitleDownloader` now manually searches for the resulting subtitle file since `yt-dlp` doesn't reliably print sidecar file paths with `--skip-download`.
- **Interactive Language Selection**: Added a prompt to ask for the subtitle language in interactive mode.
- **Real-time Progress Tracking**: Refactored `ProcessExecutor` to log `yt-dlp` output in real-time, allowing users to see download progress, speed, and ETA directly in the console.
- **Organized Storage**: Implemented automatic directory management. Files are now saved in type-specific folders: `txt/` for subtitles, `output/audio/` for audio, `output/video/` for video, and `output/img/` for screenshots.
- **Robust Path Extraction**: Enhanced `ProcessExecutor` to correctly identify absolute paths in `yt-dlp` output, ignoring warnings like "File name too long".
- **Filename Optimization**: Reduced maximum filename length to 60 characters to prevent OS-level "File name too long" errors.

## [1.3.0] - 2026-05-04

### Added
- **ProcessExecutor**: Introduced a centralized command execution system with support for timeouts (default 30 min) and improved logging.
- **Specialized Downloaders**: Split `YoutubeDownloader` into `VideoDownloader` and `SubtitleDownloader` for better SRP compliance.

### Changed
- **Reliable File Resolution**: Switched from unreliable directory listing (`Files.list(".")`) to using `yt-dlp`'s `--print after_move:filepath` to accurately locate downloaded files.
- **Main Refactoring**: Modernized task initialization in `Main.java` using Java 21 `switch` expressions and factory methods.

### Fixed
- Potential race conditions when downloading multiple files simultaneously in the same directory.
- Missing imports and minor compilation issues after refactoring.

## [1.2.0] - 2026-05-04

### Added
- **Playlist Support**: The application now automatically detects playlists and processes all contained videos sequentially.
- **Video Download Mode**: Added a new `-t video` task to download and save video files (720p max for efficiency).
- **Resilient Processing**: Errors during playlist processing no longer stop the entire execution; the tool proceeds to the next item.

## [1.1.0] - 2026-05-04

### Added
- **Architectural Refactoring**: Introduced interfaces for core components (`Downloader`, `ContentProcessor`, `FilenameProvider`, `VideoTask`) to follow the Dependency Inversion Principle (DIP).
- **Strategy Pattern**: Implemented a task-based architecture to allow different processing logic for different types of downloads.
- **Audio Download Support**: Added `AudioDownloader` and `AudioTask` to support extracting audio from YouTube videos.
- **Screenshot Capture**: Added `ScreenshotTask` and enhanced `AbstractYoutubeService` to extract periodic screenshots using `ffmpeg` fast-seeking from stream URLs.
- **Logging**: Integrated **SLF4J** and **Logback** for structured logging, replacing manual `System.out.println` calls.
- **CLI Enhancements**: Added new parameters to `Main.java` using JCommander:
  - `-t`, `--type`: Specify download type (`sub`, `audio`, or `screenshot`).
  - `--format`: Set audio format (opus, mp3, m4a).
  - `--quality`: Set audio quality (0-9).
  - `-i`, `--interval`: Set screenshot frequency in seconds.
- **Lombok Integration**: Applied Lombok annotations (`@Slf4j`, `@RequiredArgsConstructor`) across the project to reduce boilerplate code.

### Changed
- **Error Handling**: Moved `System.exit()` calls from business logic (`YtdlManager`) to the `Main` entry point.
- **Resource Management**: Optimized process output handling in `YoutubeDownloader` to prevent potential memory issues.
- **Package Structure**: Organized classes for better maintainability and extensibility.

### Fixed
- Restored missing date-parsing logic in `FilenameGenerator` that was lost during initial refactoring.
- Updated `SubtitleCleanerTest` to match the new interface-based architecture.

## [1.0.0] - 2026-05-03

### Added
- Initial Java port of the subtitle processing tool.
- Support for `yt-dlp` integration.
- VTT subtitle cleaning and formatting logic.
- Intelligent filename generation with date extraction.
- Maven build system.
- Basic unit tests for core logic.
