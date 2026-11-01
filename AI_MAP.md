# AI_MAP — карта проєкту YtdlpJava для ШІ

Оновлено: 2026-11-01 · Версія: 1.0.0 · Мова: Java 21 · Збірка: Maven

Мета файлу: швидкий контекст для агентів без повного читання репо.
Після змін оновлюй цей файл (розділи «Точки змін» і «Заборонено ламати»).

## Що це

CLI: YouTube URL → субтитри(MD) / аудіо / відео / кадри / metadata(JSON).
Зовнішні бінарники: `yt-dlp`, `ffmpeg`, `node` (у PATH).

## Точки входу

| Шлях | Роль |
|---|---|
| `com.ytdlpjava.cli.Main` | реальний CLI (JAR Main-Class) |
| `com.ytdlpjava.Main` | тонкий делегат |
| `ApplicationFactory` | єдине місце збірки залежностей |
| `YtdlManager` | плейлист + цикл відео + агрегація помилок |
| `ExternalToolsPreflight` | preflight yt-dlp/node/(ffmpeg) до завантажень |
| `LoggingConfigurator` | `-v` / `--log-level` поверх `logback.xml` |

## Пакети (залежності лише вниз)

```
cli → config → task/core → model(ports)
                    ↑
            infrastructure.*  (yt-dlp, ffmpeg, filesystem)
processor / subtitle / util  ← використовуються task і factory
```

Не імпортувати `infrastructure` з `model`.
Не створювати адаптери поза `ApplicationFactory` (крім тестів).

## Порти (`model`)

| Порт | Призначення |
|---|---|
| `MediaDownloader` | download(url, basename) → Path |
| `TitleProvider` | заголовок |
| `DurationProvider` | тривалість (screenshot) |
| `PlaylistProvider` | URL елементів + title плейлиста |
| `Downloader` | обʼєднання портів вище |
| `VideoTask` | execute(url, outputDir) |
| `FilenameProvider` | безпечні імена |
| `TemporaryFileManager` | temp write/delete |
| `ContentProcessor` / `TextProcessor` | субтитри |
| `FrameExtractor` | кадр через ffmpeg |

## Задачі (`task`)

| CLI `-t` | Клас | Примітка |
|---|---|---|
| audio / video / metadata | `DownloadAndDispatchTask` | спільний flow + `actionLabel` |
| sub | `SubtitleTask` | VTT → MD |
| screenshot | `ScreenshotTask` | interval + duration |

Спільне: `DownloadWorkspace` → `ResultDispatcher` → cleanup.

## Куди класти зміни

| Зміна | Куди |
|---|---|
| Новий тип задачі | `task/*` + case у `ApplicationFactory` + `AppConfig` |
| Новий формат завантаження | `infrastructure/ytdlp/*` extends `AbstractYoutubeService` |
| Новий обробник результату | `TaskResultHandler` + список у factory |
| Правила імен | тільки `FilenameGenerator` |
| Таймаут/процеси | тільки `ProcessExecutor` |
| Парсинг VTT | `subtitle.processor` |
| Ключові слова/мови | `processor` + `LemmatizerService` + dictionaries |

## Заборонено ламати (контракти)

1. Коди виходу CLI: `0` ok, `1` помилка обробки, `2` погані аргументи або preflight (немає yt-dlp/node/ffmpeg), `130` Ctrl+C.
1a. Preflight: завжди `yt-dlp`+`node`; `ffmpeg` лише для `audio`/`video`/`screenshot`.
2. Handlers не переміщують source — лише копіюють (`FileResultHandler`).
3. Результат download має лежати всередині `DownloadWorkspace` directory.
4. Імена: Windows reserved, `CREATE_NEW`, суфікси ` (1)`.
5. yt-dlp: `--encoding utf-8`, `--js-runtimes node`.
6. Субтитри: лише VTT.
7. `ApplicationFactory` + `LemmatizerService` закривати після batch URL.
8. `InterruptedException` не ковтати — restore interrupt.

## Тести — мінімум при зміні

| Зона | Тест-клас |
|---|---|
| Factory wiring | `ApplicationFactoryTest` |
| Isolation temp | `DownloadIsolationTest` |
| Handlers/playlist dirs | `ResultHandlingTest` |
| CLI exit codes | `MainTest` |
| Process timeout | `ProcessExecutorTest` |
| Fallback/retry | `AbstractYoutubeServiceTest` |
| Імена | `FilenameGeneratorTest` |
| VTT/MD | `SubtitleParserTest`, `SubtitleCleanerTest` |

Команда: `mvn test` (JDK 21). Інтеграції з мережею — `docs/INTEGRATION_CHECKS.md`.

## Документи

| Файл | Зміст |
|---|---|
| `README.md` | користувач / CLI |
| `docs/DOCUMENTATION.md` | архітектура |
| `docs/UML.md` | Mermaid |
| `docs/LEARNING.md` | навчання по коду |
| `PROJECT_AUDIT.txt` | останній архітектурний аудит |
| `docs/coderev.md` | історія фіксів |
| `docs/PROJECT_CONTEXT.md` | стан релізу |

## Чеклист оновлення цієї карти

- [ ] Новий клас у таблиці пакетів/задач
- [ ] Новий контракт у «Заборонено ламати»
- [ ] Посилання на тест
- [ ] Дата зверху
