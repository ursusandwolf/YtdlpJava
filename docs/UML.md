# UML YtdlpJava

Диаграммы описывают классы и сценарии в `main`, коммит `2bb5c62` от 2026-10-11.
GitHub отображает блоки Mermaid прямо в Markdown.
[Архитектура](DOCUMENTATION.md) · [README](../README.md) · [Changelog](../CHANGELOG.md)

## Основные классы и контракты

```mermaid
classDiagram
    direction LR
    class AppConfig {
        +validate() void
        +configureOutputDir() void
    }
    class ApplicationFactory {
        +createDownloader() Downloader
        +createTask() VideoTask
        +createLemmatizerService() LemmatizerService
        +close() void
    }
    class YtdlManager {
        +process(url, outputDir) void
    }
    class VideoTask {
        <<interface>>
        +execute(url, outputDir) void
        +getFilenameProvider() FilenameProvider
    }
    class PlaylistProvider {
        <<interface>>
        +getPlaylistUrls(url) List
        +getPlaylistTitle(url) String
    }
    class Downloader {
        <<interface>>
    }
    class SubtitleTask
    class AudioTask
    class VideoDownloadTask
    class ScreenshotTask
    class MetadataTask
    class ResultDispatcher {
        +dispatch(handlers, title, file, outputDir) void
    }
    class TaskResultHandler {
        <<interface>>
        +handle(title, file) void
        +handle(title, file, outputDir) void
    }
    class FileResultHandler
    class TelegramResultHandler
    class TemporaryFileManager {
        <<interface>>
        +deleteIfExists(path) void
    }
    class LemmatizerService {
        +getLemma(word, language) String
        +close() void
    }
    ApplicationFactory --> AppConfig : использует
    ApplicationFactory ..> VideoTask : создаёт
    ApplicationFactory ..> Downloader : создаёт
    ApplicationFactory *-- LemmatizerService : владеет
    YtdlManager --> VideoTask : вызывает
    YtdlManager --> PlaylistProvider : получает URL
    PlaylistProvider <|-- Downloader
    VideoTask <|.. SubtitleTask
    VideoTask <|.. AudioTask
    VideoTask <|.. VideoDownloadTask
    VideoTask <|.. ScreenshotTask
    VideoTask <|.. MetadataTask
    SubtitleTask ..> ResultDispatcher
    AudioTask ..> ResultDispatcher
    VideoDownloadTask ..> ResultDispatcher
    ScreenshotTask ..> ResultDispatcher
    MetadataTask ..> ResultDispatcher
    ResultDispatcher --> TaskResultHandler
    TaskResultHandler <|.. FileResultHandler
    TaskResultHandler <|.. TelegramResultHandler
    VideoDownloadTask --> TemporaryFileManager : очищает источник
```

Сигнатуры фабрики сокращены до имени и возвращаемого типа.
`Downloader` объединяет `PlaylistProvider` и другие узкие порты загрузки/метаданных.
Все пять задач используют временные файлы и общий dispatcher.
Фабрика создаёт анализаторы лениво и закрывается CLI после всей партии URL.

## Плейлист и итоговый статус

```mermaid
sequenceDiagram
    participant CLI as cli.Main
    participant Manager as YtdlManager
    participant Provider as PlaylistProvider
    participant Task as VideoTask
    CLI->>Manager: process(url, baseOutputDir)
    Manager->>Provider: getPlaylistUrls(url)
    Provider-->>Manager: список видео
    alt список пуст
        Manager-->>CLI: YtdlManagerException
    else список непустой
        opt плейлист
            Manager->>Provider: getPlaylistTitle(url)
            Provider-->>Manager: название
            Note over Manager: Выбрать и создать каталог плейлиста
        end
        loop каждое видео
            Manager->>Task: execute(videoUrl, effectiveOutputDir)
            alt успех
                Task-->>Manager: готово
            else обычная ошибка
                Task-->>Manager: Exception
                Note over Manager: Записать ошибку и продолжить
            end
        end
        Note over Manager: Вывести сводку
        alt были ошибки
            Manager-->>CLI: YtdlManagerException со сведениями об ошибках
        else всё успешно
            Manager-->>CLI: готово
        end
    end
    Note over CLI: Продолжить остальные URL; итоговый код 1 при любой ошибке
```

Прерывание обрабатывается отдельно: флаг interrupt сохраняется, дальнейшие элементы
и URL не запускаются. Неверные параметры отклоняются до создания адаптеров с кодом `2`.

## Жизненный цикл результата

```mermaid
sequenceDiagram
    participant Task as VideoTask
    participant Dispatch as ResultDispatcher
    participant Files as FileResultHandler
    participant Next as Следующий обработчик
    participant Temp as TemporaryFileManager
    Task->>Dispatch: dispatch(handlers, title, source, outputDir)
    Dispatch->>Files: handle(title, source, outputDir)
    Note over Files: CREATE_NEW резервирует имя; при коллизии добавить суффикс
    Files->>Files: скопировать источник
    Files-->>Dispatch: результат или ошибка
    Dispatch->>Next: handle(title, source, outputDir)
    Note over Next: Исходный файл по-прежнему доступен
    Next-->>Dispatch: результат или ошибка
    alt хотя бы один обработчик неуспешен
        Dispatch-->>Task: агрегированная Exception
    else все обработчики успешны
        Dispatch-->>Task: готово
    end
    Task->>Temp: deleteIfExists(source) в finally
```

Обычный сбой обработчика не мешает остальным. Прерывание останавливает цепочку.
Telegram-адаптер проверяет `SendResponse.isOk()`; CLI по умолчанию подключает только файлы.

## Обработка субтитров

```mermaid
flowchart LR
    VTT["VTT-файл"] --> FileProc["SubtitleFileProcessor"]
    FileProc --> Cleaner["SubtitleCleaner"]
    Cleaner --> Parser["SubtitleParser"]
    Parser --> Blocks["SubtitleBlock"]
    Blocks --> Assembler["SubtitleTextAssembler"]
    Config["SubtitleConfig"] --> Assembler
    Lines["SubtitleLineCleaner"] --> Assembler
    Assembler --> Formatter["MarkdownFormatter"]
    Formatter --> Extractor["KeywordExtractor / Lucene / словари"]
    Extractor --> Highlight["KeywordHighlighter"]
    Highlight --> MD["Markdown"]
```

Внешняя загрузка заканчивается до начала обработки текста. Все резервные стратегии
запрашивают VTT. Сборщик применяет мягкий и жёсткий пороги абзацев;
Markdown-разметка добавляется позднее.

## Внешний процесс и таймаут

```mermaid
sequenceDiagram
    participant Adapter as yt-dlp / ffmpeg adapter
    participant Executor as ProcessExecutor
    participant Child as Process
    participant Reader as Virtual thread
    Adapter->>Executor: run(command, errorMessage, timeout, unit)
    Executor->>Child: start()
    Executor->>Reader: читать stdout/stderr
    par ожидание процесса
        Executor->>Child: waitFor(оставшееся время)
    and чтение вывода
        Child-->>Reader: строки вывода
    end
    alt deadline истёк или поток прерван
        Executor->>Child: завершить доступных потомков и процесс
        Executor-->>Adapter: исключение
    else процесс завершился
        Executor->>Reader: получить вывод до того же deadline
        Reader-->>Executor: собранный вывод
        alt exit code не равен 0
            Executor-->>Adapter: ProcessExecutionException
        else успех
            Executor-->>Adapter: вывод
        end
    end
```

Таймаут по умолчанию — 30 минут. Дочерние процессы завершаются через ProcessHandle;
отделившиеся от родителя процессы могут быть недоступны этому механизму.
