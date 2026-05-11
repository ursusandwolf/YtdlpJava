# Changelog - YtdlpJava

## [Unreleased]
### Added
- Comprehensive code review findings.
- Custom exceptions for `ProcessExecutor` (`ProcessTimeoutException`, `ProcessExecutionException`).
- Global stop-words and filler dictionaries.
- 5-stage resilience fallback strategy for yt-dlp.

### Changed
- Refactored `SubtitleCleaner` to use Constructor Injection for better testability.
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
