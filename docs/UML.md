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
        |               +------v-------+             |
        |               | ResultHandler|             |
        |               +--------------+             |
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
        The `VideoTask` interface allows `YtdlManager` to process videos in different ways. All tasks now utilize `TaskResultHandler` to delegate output processing (e.g., to the filesystem via `FileResultHandler` or potentially to Telegram).

        ## Screenshot Extraction Pipeline
        `ScreenshotTask` triggers `TaskResultHandler` for each individual frame captured, allowing real-time processing/notification of progress.
