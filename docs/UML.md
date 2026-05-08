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
        |                      |      | Parser | Cleaner | Analyzer|
        |                      |      +----------------------------+
        |                      |
        +----------+-----------+
                   |
                   v
        +-----------------------+
        | com.ytdlpjava.util    |
        | (FilenameGenerator)   |
        +-----------------------+
```

## Strategy Pattern for Tasks
The `VideoTask` interface allows `YtdlManager` to process videos in different ways (subtitles, audio, video) without knowing the implementation details.

## Decomposed Processing (SubtitleCleaner)
`SubtitleCleaner` now acts as a coordinator (facade) that delegates tasks to specialized components:
1. `Parser` extracts blocks from VTT.
2. `CleanerService` removes noise (tags, fillers).
3. `MarkdownFormatter` builds the document structure.
4. `KeywordAnalyzer` highlights and summarizes key terms.
