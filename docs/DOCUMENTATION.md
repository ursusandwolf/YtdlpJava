# Архитектура YtdlpJava

Описание соответствует коду в `main`, коммит `2bb5c62` от 2026-10-11.
Запуск: [README](../README.md). Проверки и ограничения: [анализ кода](coderev.md).

## Пакеты

| Пакет `com.ytdlpjava.*` | Ответственность |
|---|---|
| `cli` | JCommander, запуск и интерактивный мастер |
| `config` | `AppConfig` и сборка зависимостей в `ApplicationFactory` |
| `core` | `YtdlManager`, внешние процессы и исключения |
| `model` | Порты загрузки, метаданных, задач, текста и временных файлов |
| `infrastructure.ytdlp` | Загрузчики и разбор вывода yt-dlp |
| `infrastructure.ffmpeg` | Извлечение кадров |
| `infrastructure.filesystem` | Жизненный цикл временных файлов |
| `task` | Пять реализаций `VideoTask` и обработчики результатов |
| `processor` | Файловая обработка субтитров, ключевые слова, Markdown |
| `subtitle` | Парсинг VTT, очистка строк и сборка абзацев |
| `util` | Имена, словари, анализаторы Lucene, текстовые утилиты |

`com.ytdlpjava.Main` делегирует запуск в `cli.Main`.
`Downloader` объединяет узкие порты, включая `MediaDownloader`, `TitleProvider`,
`DurationProvider`, `PlaylistProvider` и `StreamUrlProvider`.
Задачи получают нужные порты через конструкторы.

## Запуск и результаты

1. `cli.Main` разбирает и проверяет параметры; при отсутствии URL запускает мастер.
2. `AppConfig.configureOutputDir()` выбирает каталог для типа задачи.
3. `ApplicationFactory` создаёт загрузчик, задачу и `FileResultHandler`.
4. `YtdlManager` получает список URL и последовательно вызывает `VideoTask.execute`.
5. Задача создаёт результат, вызывает `ResultDispatcher` и очищает временные файлы.

Задачи передают эффективный каталог плейлиста в `TaskResultHandler.handle(title, file, outputDir)`.
Двухаргументный метод сохранён для совместимости. Обработчики обязаны оставлять исходный
файл доступным до завершения всех вызовов. `FileResultHandler` копирует результат,
резервируя имя через `CREATE_NEW`; коллизии получают суффикс ` (1)`, ` (2)` и далее.
`ResultDispatcher` пытается вызвать остальные обработчики после ошибки одного из них
и затем сообщает общий сбой. Прерывание останавливает обработку сразу.
Задача удаляет временный источник в `finally` после завершения обработчиков.
CLI подключает только файловый обработчик. Telegram требует подключения в коде;
его адаптер проверяет `SendResponse.isOk()` и сообщает об отказе API.

`YtdlManager` собирает ошибки элементов и после обработки плейлиста выбрасывает
`YtdlManagerException`, если были сбои или список пуст. CLI продолжает остальные URL
и возвращает `1` при любой ошибке; неправильные аргументы дают `2`, успех — `0`.

## Субтитры

```text
SubtitleDownloader → VTT → SubtitleFileProcessor → SubtitleCleaner
  → SubtitleParser → SubtitleTextAssembler → MarkdownFormatter
  → KeywordExtractor → KeywordHighlighter → временный .md → FileResultHandler
```

`SubtitleCleaner` реализует `TextProcessor`, а `SubtitleFileProcessor` — `ContentProcessor`.
Единственный `SubtitleParser` расположен в `subtitle.processor`.
`SubtitleLineCleaner` находится в `subtitle.processor`, а `SubtitleProcessor` — в `subtitle.api`.

Приложение создаёт `SubtitleConfig(180, 600, 800)`: интервал таймкодов 180 секунд,
мягкий порог абзаца 600 символов. `paragraphHardLimit` ограничивает текстовые элементы
сборщика 800 символами до добавления Markdown/подсветки. Разбиение выполняется по пробелу,
а длинные слова — по границе символов без разрыва UTF-16 surrogate pair.
Отдельный `defaultSettings()` задаёт 10 секунд.
`MarkdownFormatter` использует собственные значения 300 для разбиения предложений
и 95 для переноса строк.

`LemmatizerService` выбирает анализаторы Lucene для RU, EN, DE, ES, FR, IT, UK.
Русский и английский анализаторы выполняют стемминг, а не словарную лемматизацию;
украинский использует `UkrainianMorfologikAnalyzer`.
Загружаются общие RU/EN stop-word словари и дополнительный словарь выбранного языка.
`ApplicationFactory` владеет лениво созданным `LemmatizerService` и закрывает все
анализаторы после обработки всей партии URL. CLI использует try-with-resources,
поэтому закрытие происходит и при ошибке сборки зависимостей или обработки.

`subtitle` — пакет единого Maven-проекта, не отдельный артефакт.
Он зависит от `util.TextFormatUtils`, Lombok и Commons Text.

## Внешние процессы

`AbstractYoutubeService` добавляет `--js-runtimes node` и применяет стратегии
Default, Android, Mweb, Embedded, Desperate с настройкой попыток 3/2/2/2/1.
Реальные повторы зависят от классификации ошибки; доступность контента не гарантируется.

`ProcessExecutor` объединяет stdout/stderr, логирует строки и накапливает вывод.
Вывод читается в отдельном virtual thread, а единый таймаут по умолчанию 30 минут
охватывает ожидание процесса и завершение чтения. При таймауте или прерывании
завершаются процесс и доступные через ProcessHandle дочерние процессы.
`YtdlpOutputParser` выделяет пути из вывода; `SubtitleDownloader` ищет `.vtt`
по точному префиксу имени и языка. Все стратегии запрашивают только VTT.
Если совместимого файла нет, возвращается явная ошибка; JSON3/SRV не парсятся.

## Проверки

Тесты в `src/test/java` покрывают конфигурацию, именование, субтитры/ключевые слова,
менеджер, разбор вывода, временные файлы и Telegram-адаптер.
Результаты и недостающие проверки: [coderev.md](coderev.md).
