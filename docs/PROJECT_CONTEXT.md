# Project Context - YtdlpJava

## Current State
The project is a Java-based wrapper around `yt-dlp` for downloading and processing YouTube content (subtitles, audio, video, screenshots). It follows a clean architecture with interfaces and strategy pattern for different tasks.
## Recent Changes (2026-05-10)
- **Enhanced Reliability & Architecture**:
  - Implemented **exponential backoff retry logic** for `yt-dlp` commands to handle transient network errors.
  - Improved **Dependency Injection** by passing `ProcessExecutor` into `ScreenshotTask`.
  - Fixed a potential crash in `MarkdownFormatter` for short strings.
- **Code Quality & Reliability Improvements**:
  - Fixed English keyword stemmer to prevent aggressive truncation.
  - Refactored `ProcessExecutor` to separate `yt-dlp` specific arguments and improved path extraction by checking for file existence.
  - Updated `Main` to properly process multiple `videoUrls`.
  - Optimized regex in `KeywordAnalyzer` and migrated logging to SLF4J.
- **Advanced yt-dlp Resilience (v1.3.8)**:
  - Implemented a 5-stage "Deep Fallback" mechanism to handle "This live event has ended" and "Post-Live Manifestless mode" errors.
  - Added support for Node.js JS runtime in `ProcessExecutor` to bypass modern YouTube extraction challenges.
  - Resolved `fragment 1 not found` errors by forcing stable subtitle formats (`srv1`, `json3`) and skipping DASH/HLS manifests in fallback modes.
  - Ensured "clean starts" for all downloads using `--no-continue` and `--no-part`, preventing corrupted temporary files from blocking retries.
  - Restored VTT priority for standard videos to maintain full compatibility with the internal processing engine.
- **Keyword Extraction Quality**:
  - Expanded the Russian stop-words dictionary with common filler words (таки, примерно, давайте, говоря, хорошо, млн, год, etc.) to produce cleaner and more meaningful keyword summaries.
- **Maintenance**:
  - Updated `.gitignore` to cover all temporary `yt-dlp` file patterns.
  - Cleaned up residual temporary files from the project root.

## Pending Items
- [ ] Add integration tests that use a mock `yt-dlp` or controlled environment.
- [ ] Implement unit tests for `LemmatizerService` and `ProcessExecutor`.

  - Implemented **keyword extraction and highlighting**: automatically identifies the top 30 most frequent keywords (ignoring an expanded list of prepositions, pronouns, and common verbs) and highlights them in **bold** throughout the text.
  - Improved **filler word cleaning**: expanded detection to include "да" and implemented robust punctuation cleanup to prevent errors like ",.".
  - Enhanced **capitalization logic**: added a step to ensure correct sentence case after any punctuation cleanup or forced sentence splits.
  - Added a **Keywords Summary** section at the end of the subtitle file.
  - Switched output format to **Markdown (.md)** style, using headers for timestamps and bolding for key terms.
  - Implemented **smart line wrapping**: subtitle text is now wrapped to a maximum of 95 characters per line for better readability.
  - Enforced **sentence and paragraph limits**: sentences are now capped at 300 characters, and paragraphs are capped at 600 characters. Paragraph breaks now intelligently wait for the end of a sentence.
  - Increased **timestamp interval**: the default gap between timestamps is now 180 seconds (3 minutes) for a cleaner reading experience.
  - Fixed **block-spanning capitalization**: capitalization now correctly identifies if a new block is a continuation of a sentence from the previous block.
  - Improved **de-duplication**: scrolling lines are now correctly de-duplicated even when split across forced paragraph breaks.
  - Implemented **smart filler word cleaning**: automatically removes "э-э", "ну", "как бы" and other common fillers using Cyrillic-aware regex.
  - Implemented **smart sentence joining**: the cleaner now detects if a block is a continuation of a previous sentence and avoids incorrect capitalization.
  - Refactored `SubtitleCleaner` to join lines into paragraphs between timestamp headers.
  - Improved de-duplication of scrolling VTT lines.
  - Added comprehensive unit tests for all new cleaning features.
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

- **Playlist Folders**:
  - Implemented automatic creation of subdirectories for playlist content. `YtdlManager` now fetches the playlist title and organizes downloads into corresponding folders.
- **Subtitle Quality & Maintenance**:
  - Refactored `SubtitleCleaner` to decouple paragraph breaks from timestamp headers, strictly enforcing a 600-800 character limit per paragraph for better readability.
  - Significantly expanded the stop-words list (now `STOP_WORDS`) to exclude pronouns, common adverbs, and generic particles from keyword extraction, ensuring more meaningful summaries.
  - Improved `SubtitleCleaner` by adding more filler words ("эм", "ам", "ээ") and fixing punctuation errors where dots were incorrectly left after prepositions (e.g., "с. Победой" -> "с Победой").
  - Updated `.gitignore` to exclude `txt/` and `output/` directories.
- **Metadata Extraction**:
  - Implemented `MetadataDownloader` and `MetadataTask` to extract video description and info (tags, stats) to JSON files.
  - Added `metadata` task type (`-t metadata`) which saves files to `output/metadata/`.
- **Multi-Package Architectural Refactoring**: (2026-05-08)
  - Reorganized all classes into logical sub-packages: `core`, `model`, `downloader`, `task`, `processor`, `util`, and `ui`.
  - Moved unit tests to matching sub-packages in `src/test/java`.
  - Updated all imports and package declarations to ensure build integrity.
- **Improved English Keyword Extraction**: (2026-05-08)
  - Created a comprehensive English stop-words dictionary (`src/main/resources/dictionaries/stop_words_en.txt`).
  - Updated `SubtitleCleaner` to dynamically load stop-words based on the subtitle language.
  - Implemented basic English stemming (plurals, -ing, -ed) in `KeywordAnalyzer`.
  - Refined keyword summary to always show the top 30 most frequent words while keeping bolding for terms occurring 2+ times.
- **Major Architectural Refactoring**: (2026-05-08)
  - Refactored `SubtitleCleaner` (God Class) into specialized components: `SubtitleParser`, `SubtitleCleanerService`, `KeywordAnalyzer`, and `MarkdownFormatter` for better SRP compliance.
  - Externalized `STOP_WORDS` and `FILLERS` from code to resource files (`src/main/resources/dictionaries/`).
  - Decoupled interactive CLI logic from `Main.java` into `InteractivePromptService`.
  - Replaced wildcard imports with explicit ones across the project.
  - Consistently applied Lombok `@RequiredArgsConstructor` and `@Getter`/`@Setter`.
- **Comprehensive Code Review**: (2026-05-11)
  - Identified DIP violations in `SubtitleCleaner` (hardcoded dependencies).
  - Identified performance bottlenecks in `SubtitleCleanerService` (regex re-compilation).
  - Identified SRP violations in `KeywordAnalyzer` (analysis + highlighting mixed).
  - Recommended migrating to specific exceptions in `ProcessExecutor`.

## Pending Items
- [x] Comprehensive Code Review.
- [x] Implement Constructor Injection in `SubtitleCleaner`.
- [x] Pre-compile regex patterns in `SubtitleCleanerService` and `KeywordExtractor`.
- [x] Split `KeywordAnalyzer` into `KeywordExtractor` and `MarkdownHighlighter`.
- [x] Remove FQNs and optimize imports across the project.
- [x] Refactor `Main` to improve service initialization and SRP.
- [ ] Add integration tests that use a mock `yt-dlp` or controlled environment.
- [ ] Implement unit tests for `ProcessExecutor`.
