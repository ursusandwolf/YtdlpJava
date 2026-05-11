# UML Diagrams - YtdlpJava

## Package-Based Architecture

```text
+-----------------------------------------------------------+
|                      com.ytdlpjava                        |
|   +------+       +---------------------------+            |
|   | Main |------>| InteractivePromptService  | (ui)       |
|   +------+       +---------------------------+            |
|       |                                                   |
+-------|---------------------------------------------------+
        v
+-----------------------------------------------------------+
|                    com.ytdlpjava.core                     |
|   +-----------------+        +-----------------+          |
|   |   YtdlManager   |------->| ProcessExecutor |          |
|   +-----------------+        +-----------------+          |
+-------|---------------------------------------------------+
        v
+-----------------------------------------------------------+
|                    com.ytdlpjava.model                    |
|   +------------+    +-----------+    +------------------+ |
|   | Downloader |    | VideoTask |    | ContentProcessor | |
|   +------------+    +-----------+    +------------------+ |
+-------|----------------------|-------------------|--------+
        v                      v                   v
+----------------+     +---------------+    +-------------------+
| .downloader    |     | .task         |    | .processor        |
| (impls)        |     | (impls)       |    | (logic)           |
+----------------+     +---------------+    +-------------------+
        |                      |                     |
        |                      |            +--------+----------+
        |                      |            | SubtitleCleaner   |
        |                      |            +--------+----------+
        |                      |                     |
        |                      |      +--------------+-------------+
        | Parser | Cleaner | Analyzer|
        +----------------------------+
                   |
                   v
          +-----------------------+
          | com.ytdlpjava.util    |
          | (FilenameGenerator)   |
          | (LemmatizerService)   |
          +-----------------------+

        ## Strategy Pattern for Tasks
        The `VideoTask` interface allows `YtdlManager` to process videos in different ways. `ScreenshotTask` now utilizes an injected `ProcessExecutor` for frame extraction.

        ## Decomposed Processing (SubtitleCleaner)
        `SubtitleCleaner` delegates to:
        1. `Parser` extracts blocks from VTT.
        2. `CleanerService` removes noise.
        3. `MarkdownFormatter` builds the document structure.
        4. `KeywordAnalyzer` highlights terms using `LemmatizerService` for accurate stemming.

        > **Note (Code Review 2026-05-11):** Current implementation of `SubtitleCleaner` uses hardcoded dependencies. Migration to full Dependency Injection is planned to improve testability. `KeywordAnalyzer` is also targeted for splitting into extraction and formatting layers.
