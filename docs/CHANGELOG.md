# Changelog - YtdlpJava

## [1.5.1] - 2026-05-20
### Added
- Added `cli`, `config`, and `infrastructure` packages to separate application wiring from runtime adapters.
- Added explicit ports for media download, playlist lookup, title resolution, duration lookup, frame extraction, temporary files, and text processing.
- Added tests for `AppConfig` and `FilesystemTemporaryFileManager`.

### Changed
- Moved yt-dlp implementations from `downloader` to `infrastructure.ytdlp`.
- Moved interactive CLI handling to `cli` and retained `com.ytdlpjava.Main` as a compatibility delegate.
- Simplified `cli.Main` by parsing `AppConfig` directly instead of duplicating CLI fields.
- Updated `ScreenshotTask` to use `FrameExtractor` and `TemporaryFileManager`.
- Refreshed architecture docs and UML to reflect the layered package layout.

## [1.5.0] - 2026-05-19
### Added
- **Universal Task Result Handling**:
  - Implemented `TaskResultHandler` interface to decouple output processing (files, notifications, etc.) from task logic.
  - Added `FileResultHandler` for standard filesystem output.
  - Generalized all tasks (`SubtitleTask`, `AudioTask`, `VideoDownloadTask`, `ScreenshotTask`, `MetadataTask`) to support multiple result handlers.
- **Enhanced Test Reliability**:
  - Fixed `SubtitleCleanerTest.testKeywordExclusion` by using more unique terms to avoid overlap with the extensive stop-word dictionary.

### Changed
- Refactored `SubtitleTask` to use temporary files for intermediate processing before handing off to result handlers.
- Updated `Main.java` to inject `FileResultHandler` into all task types.

## [1.4.0] - 2026-05-13
### Changed
- **Architectural Cleanup**:
  - Removed all Fully Qualified Names (FQNs) in favor of explicit imports across the codebase.
  - Refactored `Main.java` to extract service initialization into specialized factory methods.
  - Enforced Dependency Injection (DI) in `SubtitleCleaner` using Lombok `@RequiredArgsConstructor`.
  - Optimized Regex performance by moving patterns to static constants in `KeywordExtractor` and `SubtitleCleanerService`.
  - Refined `ProcessExecutor` path extraction logic to reduce unnecessary object allocations.
- **Testing**:
  - Updated `SubtitleCleanerTest` and `KeywordAnalyzerTest` to support new constructor-based dependency injection.
  - Fixed test failures caused by incorrect stop-word filtering in unit tests.

## [Unreleased]
### Added
- Comprehensive code review findings.
- Custom exceptions for `ProcessExecutor` (`ProcessTimeoutException`, `ProcessExecutionException`).
- `YtdlManagerException` for domain-specific error handling.
- Global stop-words and filler dictionaries.
- 5-stage resilience fallback strategy for yt-dlp.

### Changed
- Refactored `SubtitleCleaner` to use Constructor Injection for better testability.
- Refactored `YtdlManager` to throw `YtdlManagerException` and improved exception logging.
- Optimized `SubtitleCleanerService` by pre-compiling regex patterns.
- Split `KeywordAnalyzer` into `KeywordExtractor` and `KeywordHighlighter`.

### Fixed
- English keyword extraction stemming rules.
- Markdown formatting for short strings.
- Path resolution for subtitles.

## [1.3.8] - 2026-05-10
### Added
- Exponential backoff retry logic.
- Node.js runtime support for yt-dlp.
- Support for stable subtitle formats (srv1, json3).

## [1.3.0] - 2026-05-08
### Changed
- Major architectural refactoring into sub-packages.
- Refactored `SubtitleCleaner` into specialized services.
- Migrated logging to SLF4J.
