# Documentation - YtdlpJava

## Overview
YtdlpJava is a specialized tool for interacting with YouTube content using `yt-dlp`. It follows a layered architecture to provide clean separation between downloading, processing, and UI logic.

## Architecture & Packages

### `com.ytdlpjava.core`
The heartbeat of the application.
- `YtdlManager`: Orchestrates the download process, handles playlists, and executes tasks.
- `ProcessExecutor`: Manages external command execution (`yt-dlp`, `ffmpeg`) with real-time logging and timeouts.

### `com.ytdlpjava.model`
Core abstractions and interfaces.
- `Downloader`: Interface for yt-dlp operations.
- `VideoTask`: Strategy interface for different processing pipelines.
- `ContentProcessor`: Interface for post-download data transformation.
- `FilenameProvider`: Interface for name generation.

### `com.ytdlpjava.processor`
Advanced content processing logic (primarily for subtitles).
- `SubtitleCleaner`: A facade that coordinates parsing, cleaning, analysis, and formatting.
- `SubtitleParser`: VTT/SRT parsing.
- `SubtitleCleanerService`: Removal of HTML tags, speaker tags, and filler words.
- `KeywordAnalyzer`: Multi-language keyword extraction and stemming (RU/EN).
- `MarkdownFormatter`: Markdown transformation, line wrapping, and length enforcement.

### `com.ytdlpjava.downloader`
Implementations of the `Downloader` interface.
- `AbstractYoutubeService`: Base class with a 5-stage resilience fallback strategy:
  1. Default with retries.
  2. Android client.
  3. Mweb client.
  4. Embedded client.
  5. Desperate mode (skip dash/hls).
- `SubtitleDownloader`: Specialized for sidecar subtitle files.
- `AudioDownloader`: Optimized for audio extraction (opus, mp3, m4a).
- `VideoDownloader`: Standard video downloads.
- `MetadataDownloader`: JSON metadata extraction.

## Technical Debt & Road Map
Following the code review on 2026-05-11:
- **Dependency Injection**: Plan to move `SubtitleCleaner` to full DI.
- **Optimization**: Pre-compile regex in `SubtitleCleanerService`.
- **Modularity**: Split analysis and highlighting in `KeywordAnalyzer`.

### `com.ytdlpjava.task`
Concrete implementations of `VideoTask`.
- `SubtitleTask`: Pipeline for subtitle processing.
- `AudioTask`: Pipeline for audio extraction.
- `ScreenshotTask`: Captures periodic screenshots using ffmpeg fast-seeking.
- `MetadataTask`: Saves descriptions and tags as JSON.

### `com.ytdlpjava.ui`
- `InteractivePromptService`: Handles the terminal-based interactive wizard.

### `com.ytdlpjava.util`
- `DictionaryLoader`: Resource-based loader for stop-words and fillers.
- `FilenameGenerator`: Date-aware file naming logic.
- `LemmatizerService`: Multi-language stemming service (supports Russian Porter stemmer and simplified English rules).
- `RussianStemmer`: Implementation of the Porter stemming algorithm for Russian.

## Usage
Run the application with a YouTube URL as the first argument, or provide it via interactive prompt.
Use `-t` to specify the task type (`sub`, `audio`, `video`, `screenshot`, `metadata`).
Subtitles are saved to `txt/`, other assets to `output/`.
