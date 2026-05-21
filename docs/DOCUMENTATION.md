# Documentation - YtdlpJava

## Overview
YtdlpJava is a specialized tool for interacting with YouTube content using `yt-dlp`. It follows a layered architecture to provide clean separation between downloading, processing, and UI logic.

## Architecture & Packages

### `com.ytdlpjava.cli`
Command-line entrypoint and interactive prompt.
- `Main`: CLI parsing and application startup.
- `InteractivePromptService`: Terminal-based interactive wizard.
- `com.ytdlpjava.Main`: Backward-compatible delegating entrypoint.

### `com.ytdlpjava.config`
Composition root and runtime configuration.
- `AppConfig`: Parsed CLI/interactive configuration.
- `ApplicationFactory`: Wires application ports to infrastructure implementations.

### `com.ytdlpjava.core`
The heartbeat of the application.
- `YtdlManager`: Orchestrates the download process, handles playlists, and executes tasks.
- `ProcessExecutor`: Manages external command execution with real-time logging and timeouts.

### `com.ytdlpjava.model`
Core abstractions and interfaces.
- `Downloader`: Interface for yt-dlp operations.
- Narrow ports: `MediaDownloader`, `TitleProvider`, `DurationProvider`, `PlaylistProvider`, `StreamUrlProvider`.
- `VideoTask`: Strategy interface for different processing pipelines.
- `ContentProcessor` / `TextProcessor`: Interfaces for post-download data transformation.
- `FilenameProvider`: Interface for name generation.
- `FrameExtractor`: Interface for extracting frames from video files.
- `TemporaryFileManager`: Interface for temporary file lifecycle.

### `com.ytdlpjava.subtitle`
Standalone, decoupled module for subtitle processing.
- `api.SubtitleProcessor`: Interface for assembly pipelines.
- `api.SubtitleLineCleaner`: Interface for text cleaning strategies.
- `model.SubtitleBlock`: Immutable record for subtitle segments.
- `config.SubtitleConfig`: Parameter object for processing thresholds.
- `processor.SubtitleTextAssembler`: Core implementation of the subtitle assembly logic.
- `processor.SubtitleParser`: Shared VTT parser.

### `com.ytdlpjava.processor`
Advanced content processing logic (primarily for subtitles).
- `SubtitleCleaner`: A facade that implements `TextProcessor`. It coordinates analysis and highlighting, utilizing the `com.ytdlpjava.subtitle` module for parsing and assembly.
- `SubtitleFileProcessor`: Implements `ContentProcessor`. Acts as a bridge between file IO and `SubtitleCleaner` (ISP compliance).
- `KeywordAnalyzer`: Multi-language keyword extraction and stemming (RU/EN).
- `MarkdownFormatter`: Markdown transformation, line wrapping, and length enforcement.

### `com.ytdlpjava.infrastructure.ytdlp`
yt-dlp adapters and output parsing.
- `AbstractYoutubeService`: Base class with a 5-stage declarative "Fallback Strategy":
  1. Default (3 retries).
  2. Android client (2 retries).
  3. Mweb client (2 retries).
  4. Embedded client (2 retries).
  5. Desperate mode (skip dash/hls, 1 retry).
- `SubtitleDownloader`: Specialized for sidecar subtitle files.
- `AudioDownloader`: Optimized for audio extraction (opus, mp3, m4a).
- `VideoDownloader`: Standard video downloads.
- `MetadataDownloader`: JSON metadata extraction.
- `YtdlpOutputParser`: Parses yt-dlp output for downloaded file paths.

### `com.ytdlpjava.infrastructure.ffmpeg`
- `FfmpegFrameExtractor`: ffmpeg-backed implementation of `FrameExtractor`.

### `com.ytdlpjava.infrastructure.filesystem`
- `FilesystemTemporaryFileManager`: filesystem-backed temporary file lifecycle.

## Technical Debt & Road Map
Updated: 2026-05-19
- **Dependency Injection**: Plan to move `SubtitleCleaner` to full DI.
- **Optimization**: (Completed) Pre-compile regex in `SubtitleCleanerService`.
- **Modularity**: (Completed) Split analysis and highlighting in `KeywordAnalyzer`.
- **Pending**:
  - Implement Dependency Injection for `SubtitleCleaner`.
  - Migrate `LemmatizerService` to Lucene.
  - Decouple formatting from `SubtitleCleaner` into `MarkdownFormatter`.
  - Add integration tests for `yt-dlp`.
  - Unit tests for `ProcessExecutor`.

### `com.ytdlpjava.task`
Concrete implementations of `VideoTask`. All tasks now support `TaskResultHandler` for flexible output routing.
- `TaskResultHandler`: Interface for handling processing results (e.g., saving to file, sending to Telegram).
- `FileResultHandler`: Default implementation that moves result files to the final output directory.
- `TelegramResultHandler`: New implementation for sending results to Telegram bots via `java-telegram-bot-api`.
- `SubtitleTask`: Pipeline for subtitle processing. Now uses temporary files for intermediate Markdown result.
- `AudioTask`: Pipeline for audio extraction.
- `ScreenshotTask`: Captures periodic screenshots. Handlers are invoked in real-time for each captured frame.
- `MetadataTask`: Saves descriptions and tags as JSON.
- `VideoDownloadTask`: Handles standard video file downloads.

### `com.ytdlpjava.util`
- `DictionaryLoader`: Resource-based loader for stop-words and fillers.
- `FilenameGenerator`: Date-aware file naming logic.
- `LemmatizerService`: Multi-language lemmatization using **Apache Lucene** (Russian and English analyzers).
- `TextFormatUtils`: Shared utilities for formatting and text analysis.

## Usage
Run the application with a YouTube URL as the first argument, or provide it via interactive prompt.
Use `-t` to specify the task type (`sub`, `audio`, `video`, `screenshot`, `metadata`).
Subtitles are saved to `txt/`, other assets to `output/`.
