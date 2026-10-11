<div align="center">

# YtdlpJava

**От YouTube-ссылки до текста, аудио и кадров — из командной строки.**

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square)
![Maven](https://img.shields.io/badge/build-Maven-C71A36?style=flat-square)
![CLI](https://img.shields.io/badge/interface-CLI-2D6A4F?style=flat-square)

[Быстрый старт](#быстрый-старт) · [Примеры](#примеры) · [Параметры](#параметры-cli) · [Архитектура](docs/DOCUMENTATION.md) · [UML](docs/UML.md) · [Changelog](CHANGELOG.md)

</div>

YtdlpJava — приложение на Java 21, которое использует `yt-dlp` для получения
YouTube-контента и `ffmpeg` для обработки медиа. Оно превращает VTT-субтитры
в читаемый Markdown, извлекает аудио, сохраняет видео и метаданные,
создаёт серии кадров. Можно обработать одно видео, несколько ссылок или плейлист.

## Что умеет проект

| Задача | Результат | Каталог по умолчанию |
|---|---|---|
| **Субтитры** | Markdown: очищенный текст, таймкоды, выделенные ключевые слова и их список | `txt/` |
| **Аудио** | OPUS, MP3 или M4A с выбранным качеством | `output/audio/` |
| **Видео** | Видеофайл с предпочтением MP4 до 720p | `output/video/` |
| **Кадры** | JPEG через заданный интервал | `output/img/` |
| **Метаданные** | JSON с данными видео | `output/metadata/` |

Для ключевых слов настроены анализаторы и словари семи языков:
**английского, русского, украинского, немецкого, испанского, французского и итальянского**.

Результаты плейлистов группируются по названию. Совпадающие имена получают
суффиксы ` (1)`, ` (2)` и далее, поэтому предыдущие файлы сохраняются.
Ошибки отдельных видео не мешают обработать остальные ссылки.

## Быстрый старт

### 1. Подготовьте инструменты

| Инструмент | Для чего нужен |
|---|---|
| **JDK 21** | Сборка и запуск приложения |
| **Maven** | Зависимости и тесты |
| **yt-dlp** | Контент, субтитры и метаданные |
| **ffmpeg** | Аудио, объединение потоков и извлечение кадров |
| **Node.js** | JS runtime: приложение передаёт `--js-runtimes node` в yt-dlp |

Команды должны быть доступны в `PATH`. Проверить окружение можно так:

```shell
java -version
mvn -version
yt-dlp --version
ffmpeg -version
node --version
```

### 2. Получите исходники и соберите проект

Основная ветка с кодом приложения — `main`.

```shell
git clone --branch main https://github.com/ursusandwolf/YtdlpJava.git
cd YtdlpJava
mvn clean verify
mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
```

### 3. Запустите приложение

**Windows / PowerShell**

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main
```

**Linux / macOS**

```shell
java -cp "target/classes:target/dependency/*" com.ytdlpjava.cli.Main
```

Без URL откроется интерактивный мастер: он предложит выбрать ссылку, задачу
и язык субтитров или формат аудио. Для справки добавьте `--help`.

> Текущая Maven-сборка создаёт обычный JAR без `Main-Class` и включённых зависимостей.
> Используйте classpath-команды выше. Запуск через `java -jar` пока не предусмотрен.
> После `mvn clean` папку runtime-зависимостей нужно подготовить заново.

## Примеры

Команды ниже предназначены для PowerShell. В Linux / macOS замените
`;` между элементами classpath на `:`.
Замените `VIDEO_ID` и `PLAYLIST_ID` на свои идентификаторы.

### Субтитры на русском

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://youtu.be/VIDEO_ID" -t sub -l ru
```

### Аудио в MP3

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://youtu.be/VIDEO_ID" -t audio --format mp3 --quality 0
```

### Кадр каждые 30 секунд

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://youtu.be/VIDEO_ID" -t screenshot -i 30
```

### Видео из плейлиста

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://www.youtube.com/playlist?list=PLAYLIST_ID" -t video
```

### Метаданные в отдельный каталог

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://youtu.be/VIDEO_ID" -t metadata -o downloads/metadata
```

### Несколько ссылок за один запуск

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://youtu.be/FIRST_ID" "https://youtu.be/SECOND_ID" -t sub -l de
```

## Параметры CLI

| Параметр | Назначение | По умолчанию |
|---|---|---|
| URL | Одна или несколько позиционных ссылок | Интерактивный ввод |
| `-t`, `--type` | `sub`, `audio`, `video`, `screenshot`, `metadata` | `sub` |
| `-o`, `--output_dir` | Каталог результата | Зависит от задачи |
| `-l`, `--lang` | Код языка субтитров | `en` |
| `--format` | `opus`, `mp3`, `m4a` | `opus` |
| `--quality` | Целое число: `0` — лучшее, `9` — худшее | `0` |
| `-i`, `--interval` | Положительный интервал кадров, секунды | `60` |
| `-h`, `--help` | Справка | — |

Коды языков с настроенным анализом: `en`, `ru`, `uk`, `de`, `es`, `fr`, `it`.
Для других кодов применяется английский анализатор; отдельного словаря может не быть.
Наличие субтитров выбранного языка зависит от видео.

`-o output` преобразуется в стандартный каталог задачи. Любой другой путь,
например `-o downloads`, используется как указано. Для плейлиста внутри него
создаётся папка с обработанным названием, если удалось получить название.

### Коды завершения

| Код | Значение |
|---|---|
| `0` | Обработка завершилась без ошибок или показана справка |
| `1` | Ошибка обработки хотя бы одного элемента, ошибка приложения или пустой список видео |
| `2` | Неверные аргументы |

Сводка успешных и неуспешных элементов выводится в лог.
Прерывание останавливает дальнейшую обработку.

## Как устроен проект

```mermaid
flowchart LR
    CLI["CLI / интерактивный мастер"] --> Manager["Менеджер видео и плейлистов"]
    Manager --> Task["Задача обработки"]
    Task --> Media["yt-dlp / ffmpeg"]
    Task --> Text["VTT → Markdown"]
    Task --> Dispatch["Обработчики результата"]
    Dispatch --> Files["Файлы без перезаписи"]
    Dispatch --> Telegram["Telegram: подключение в коде"]
```

`ApplicationFactory` собирает зависимости, задачи используют узкие интерфейсы,
а инфраструктурные адаптеры вызывают внешние инструменты. Исходный результат
доступен всем обработчикам, затем задача очищает временные файлы.
Анализаторы текста живут до завершения всей партии URL и закрываются при выходе.

[Описание архитектуры](docs/DOCUMENTATION.md) · [UML классов и последовательностей](docs/UML.md)

## Проверки и разработка

```shell
mvn clean verify
```

Проверка от 2026-10-11 завершилась успешно: **58 тестов**, без ошибок, падений
и пропусков. Набор покрывает таймауты реальных дочерних Java-процессов,
каталоги плейлистов, параллельные коллизии файлов, обработчики результатов,
проверки CLI, ответы Telegram через mock и обработку текста.
Результаты проверки и подробности исправлений: [анализ кода](docs/coderev.md).

Для изменений добавляйте проверки соответствующего сценария,
обновляйте [changelog](CHANGELOG.md) и документацию затронутого поведения.
Исправления опубликованы в `main` коммитом `2bb5c62`.
[Итоги слияния](docs/MERGE.md) · [Состояние проекта и дальнейшие шаги](docs/PROJECT_CONTEXT.md)

## Ограничения

- Поддерживаются только VTT-субтитры. Если VTT недоступен, приложение сообщает об ошибке.
- Последний резервный формат видео `best` может превышать 720p.
- Telegram-адаптер проверяет ответ API, но CLI пока не предоставляет флаги токена и chat ID.
- Предел абзаца применяется до добавления Markdown-разметки.
- Реальная загрузка YouTube, обработка ffmpeg и отправка Telegram не входят
  в выполненные локальные проверки.
- Используется SLF4J/Logback; собственного `logback.xml` в репозитории нет.

## Документация

| Документ | Содержание |
|---|---|
| [Архитектура](docs/DOCUMENTATION.md) | Пакеты, зависимости и потоки данных |
| [UML](docs/UML.md) | Классы, обработка плейлистов и жизненный цикл результата |
| [Changelog](CHANGELOG.md) | Единая история изменений |
| [Анализ кода](docs/coderev.md) | Исправленные находки, проверки и ограничения |
| [Контекст проекта](docs/PROJECT_CONTEXT.md) | Текущее состояние и задачи к выпуску |
| [Итоги слияния](docs/MERGE.md) | Выполненное слияние и архив подготовительного документа |
| [Пакет субтитров](src/main/java/com/ytdlpjava/subtitle/README.md) | API обработки VTT |

## Лицензия

Файл лицензии пока отсутствует. До оформления лицензии проект не заявляется как MIT.
