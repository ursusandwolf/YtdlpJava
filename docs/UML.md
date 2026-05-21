# UML Diagrams - YtdlpJava

## Package-Based Architecture

```text
+-----------------------------------------------------------+
|                      com.ytdlpjava                        |
|   +------+                                               |
|   | Main | (backward-compatible delegate)                 |
|   +------+                                               |
|       |                                                   |
+-------|---------------------------------------------------+
        v
+-----------------------------------------------------------+
|                    com.ytdlpjava.cli                      |
|   +------+       +---------------------------+            |
|   | Main |------>| InteractivePromptService  |            |
|   +------+       +---------------------------+            |
+-------|---------------------------------------------------+
        v
+-----------------------------------------------------------+
|                    com.ytdlpjava.config                   |
|   +-------------+       +--------------------+            |
|   |  AppConfig  |------>| ApplicationFactory |            |
|   +-------------+       +--------------------+            |
+-------|----------------------|-------------------|--------+
        v                      v
+----------------+     +-----------------------------------------+
| .core          |     | .model                                 |
| YtdlManager    |     | ports: VideoTask, MediaDownloader,     |
| ProcessExecutor|     | FrameExtractor, TemporaryFileManager   |
+----------------+     +-----------------------------------------+
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
          | .subtitle             |
          | Parser | Assembler    |
          | Config | Block        |
          +-----------------------+
                   |
                   v
          +-----------------------+
          | infrastructure        |
          | ytdlp / ffmpeg / fs   |
          +-----------------------+


        ## Strategy Pattern for Tasks
        The `VideoTask` interface allows `YtdlManager` to process videos in different ways. All tasks now utilize `TaskResultHandler` to delegate output processing (e.g., to the filesystem via `FileResultHandler` or potentially to Telegram).

        ## Screenshot Extraction Pipeline
        `ScreenshotTask` depends on `FrameExtractor`; the ffmpeg command is implemented by `FfmpegFrameExtractor` in infrastructure.
