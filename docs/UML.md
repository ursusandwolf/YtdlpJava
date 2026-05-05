# UML Diagrams - YtdlpJava

## Class Diagram

```
+------------------+       1 +-------------+
|      Main        |-------->| YtdlManager |
+------------------+         +-------------+
        |                           |
        | uses                      | uses
        v                           v
+------------------+         +-------------+
| ProcessExecutor  |         |  VideoTask  | (Interface)
+------------------+         +-------------+
                                   ^
                                   |
                +------------------+------------------+
                |                  |                  |
        +--------------+   +--------------+   +--------------+
        | SubtitleTask |   |  AudioTask   |   | ScreenshotTask|
        +--------------+   +--------------+   +--------------+

+-------------+
| Downloader  | (Interface)
+-------------+
      ^
      |
+--------------------------+
| AbstractYoutubeService   |
+--------------------------+
      ^
      |
+-----+--------------------+-----------------------+
|                          |                       |
+-------------------+ +-------------------+ +-------------------+
| VideoDownloader   | | AudioDownloader   | | SubtitleDownloader|
+-------------------+ +-------------------+ +-------------------+
```

## Strategy Pattern for Tasks
The `VideoTask` interface allows `YtdlManager` to process videos in different ways (subtitles, audio, video) without knowing the implementation details.
