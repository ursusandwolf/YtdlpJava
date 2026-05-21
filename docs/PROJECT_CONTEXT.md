# Project Context - YtdlpJava

## Current State
The project is a Java-based wrapper around `yt-dlp` for downloading and processing YouTube content (subtitles, audio, video, screenshots). It follows a clean architecture with interfaces and strategy pattern for different tasks.

## Recent Changes (2026-05-21)
- **Decoupled Subtitle Module (v1.6.0)**:
  - Refactored subtitle processing into a standalone `com.ytdlpjava.subtitle` package.
  - Introduced `SubtitleConfig` for better parameterization and `SubtitleProcessor`/`SubtitleLineCleaner` interfaces for extensibility.
  - Merged duplicate parser logic and improved modularity.
- **Architectural Cleanup & Code Review Fixes**:
  - Migrated `LemmatizerService` to **Apache Lucene**, eliminating the custom `RussianStemmer` while increasing accuracy.
  - Consolidated shared text utilities into `TextFormatUtils`.
  - Replaced manual HTML unescaping with `StringEscapeUtils.unescapeHtml4`.
  - Refactored `AbstractYoutubeService` to use a declarative "Fallback Strategy" pattern.
- **ISP & Logic Refinement**:
  - Split `SubtitleCleaner` into `SubtitleCleaner` (TextProcessor) and `SubtitleFileProcessor` (ContentProcessor) to satisfy ISP.
  - Eliminated duplicate assembly logic by ensuring `SubtitleCleaner` uses the `subtitle` module.

## Recent Changes (2026-05-19)
- **Universal Task Result Handling (v1.5.0)**:
  - Implemented `TaskResultHandler` and `FileResultHandler` to decouple IO from processing tasks.
  - Generalized all `VideoTask` implementations (`Subtitle`, `Audio`, `Video`, `Screenshot`, `Metadata`) to support multiple result handlers.

## Pending Items
- [x] Comprehensive Code Review (completed 2026-05-19).
- [x] Implement Dependency Injection (or Builder pattern) for `SubtitleCleaner`.
- [x] Migrate `LemmatizerService` to use Lucene instead of custom `RussianStemmer`.
- [x] Decouple formatting logic from `SubtitleCleaner` into `MarkdownFormatter`.
- [x] Merge duplicate `SubtitleParser` implementations.
- [x] Split `KeywordHighlighter` into highlighting and summary generation.
- [ ] Improve `ProcessExecutor` path extraction (use `--print` or JSON metadata).
- [ ] Add integration tests that use a mock `yt-dlp` or controlled environment.
- [ ] Implement unit tests for `ProcessExecutor`.

## Historical Highlights
- **Universal Task Result Handling (v1.5.0)**: Decoupled output processing (Files, Telegram) from task logic.
- **Advanced yt-dlp Resilience (v1.3.8)**: 5-stage "Deep Fallback" mechanism and Node.js runtime support.
- **Multi-Package Refactoring (2026-05-08)**: Clean architecture layout with logical sub-packages.
