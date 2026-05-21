# Subtitle Module (Decoupled)

The subtitle processing logic has been refactored into a standalone module (`com.ytdlpjava.subtitle.*`).

- **Decoupling**: No dependencies on `com.ytdlpjava.model` or other domain-specific processors.
- **Modularity**: Logic is split into `api` (interfaces), `model` (simple records), `config` (parameter objects), and `processor` (implementations).
- **Extensibility**: Can be easily ported to other projects by copying the `subtitle` package.

## Components
- `SubtitleBlock`: Immutable record representing a subtitle segment.
- `SubtitleConfig`: Configuration parameters for processing.
- `SubtitleProcessor`: Interface for custom assembly pipelines.
- `SubtitleLineCleaner`: Interface for text cleaning strategies.
