# Changelog - YtdlpJava

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
