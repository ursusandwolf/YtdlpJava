# Changelog - YtdlpJava

## [Unreleased]
### Added
- Comprehensive code review findings.
- Global stop-words and filler dictionaries.
- 5-stage resilience fallback strategy for yt-dlp.

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
