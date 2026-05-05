# Documentation - YtdlpJava

## Overview
YtdlpJava is a specialized tool for interacting with YouTube content using `yt-dlp`. It provides a structured way to download and process different types of media assets.

## Core Components

### Downloader
Interface for interacting with `yt-dlp`.
- `download(url, output)`: Downloads the content.
- `getTitle(url)`: Fetches the video title.
- `getPlaylistUrls(url)`: Expands a playlist into individual video URLs.

### VideoTask
Strategy interface for processing a single video URL.
- `SubtitleTask`: Downloads and cleans subtitles.
- `AudioTask`: Extracts audio in specified format/quality.
- `VideoDownloadTask`: Downloads video (max 720p).
- `ScreenshotTask`: Captures periodic screenshots from the stream.

### YtdlManager
Orchestrator that handles playlist expansion and task execution.

## Usage
Run the application with a YouTube URL as the first argument, or provide it via interactive prompt.
Use `-t` to specify the task type (`sub`, `audio`, `video`, `screenshot`).
