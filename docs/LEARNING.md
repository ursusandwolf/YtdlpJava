# Навчальні матеріали: читаємо код YtdlpJava

Мета: зрозуміти проєкт за 1–2 години. Читай у цьому порядку.

## 1. Шлях новачка (30 хв)

1. `README.md` — що вміє CLI.
2. `cli/Main.java` — парсинг, коди виходу, try-with-resources factory.
3. `config/ApplicationFactory.java` — хто кого створює.
4. `core/YtdlManager.java` — плейлист і помилки елементів.
5. `task/DownloadAndDispatchTask.java` — найпростіша задача.
6. `task/DownloadWorkspace.java` — чому temp каталог обовʼязковий.

## 2. Порти vs адаптери (ключова ідея)

**Порт** (`model.MediaDownloader`) — «мені потрібен файл з URL».
**Адаптер** (`VideoDownloader`) — «зроблю це через yt-dlp».

Задача знає лише порт. Тести підставляють лямбду замість yt-dlp.
Це і є Ports & Adapters без Spring.

Вправа: у `DownloadIsolationTest` знайди анонімний `MediaDownloader` і поясни,
чому тест не потребує мережі.

## 3. Пʼять задач — три реалізації

```
audio  ─┐
video  ─┼─► DownloadAndDispatchTask(actionLabel)
metadata┘

sub        ► SubtitleTask (+ ContentProcessor)
screenshot ► ScreenshotTask (+ Duration + FrameExtractor)
```

Чому так: audio/video/metadata відрізняються лише завантажувачем і логом,
не алгоритмом збереження.

## 4. Життєвий цикл файлу

```
workspace.download  →  temp file
ResultDispatcher    →  FileResultHandler копіює (CREATE_NEW)
finally             →  delete temp
workspace.close     →  delete весь temp dir (включно .part)
```

Правило: handler **ніколи** не `move` source — інакше зламає наступний handler.

## 5. Субтитри як pipeline

```
SubtitleDownloader → .vtt
SubtitleFileProcessor → читає файл
SubtitleCleaner → parse + assemble + keywords + markdown
тимчасовий .md → FileResultHandler → txt/
```

`SubtitleConfig(180, 600, 800)`: інтервал таймкодів / мʼякий / жорсткий ліміт абзацу.
Мови: Lucene analyzers у `LemmatizerService` + stop-words у `resources/dictionaries`.

## 6. Стійкість до YouTube

`AbstractYoutubeService.runResiliently`:
Default → Android → Mweb → Embedded → Desperate
+ retries на transient (503, timeout, …).

`ProcessExecutor`: virtual thread читає stdout; один timeout на процес+читання;
вбиває дерево процесів при дедлайні.

## 7. Помилки і коди виходу

| Ситуація | Код |
|---|---|
| Успіх | 0 |
| Хоч один URL/елемент впав | 1 |
| Погані CLI-аргументи | 2 |
| Ctrl+C після cleanup | 130 |

`YtdlManager` збирає failures елементів і кидає `YtdlManagerException`.
CLI логує і йде до наступного URL.

## 8. Вправи для закріплення

1. Додай тип `-t thumb` (лише уявний план): які 4 файли змінити?
2. Чому `getTitle` при помилці повертає `Unknown_Video_*`, а `getDuration` кидає?
3. Зламай навмисно: handler робить `Files.move` — який тест має впасти?
4. Знайди, де екрануються `%` у імені для yt-dlp і навіщо.

## 9. Що читати далі

- `docs/DOCUMENTATION.md` — повна карта пакетів
- `docs/UML.md` — Mermaid sequence
- `AI_MAP.md` — правила для ШІ при правках
- `PROJECT_AUDIT.txt` — вердикт архітектора
