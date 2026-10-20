# Реальная проверка видео — 2026-10-20

Проверено указанное пользователем видео:
https://www.youtube.com/shorts/N3vr7ir5BXc

Заголовок: «Все времена в немецком». Длительность в метаданных — 163 секунды.
Все пять задач запускались через `com.ytdlpjava.cli.Main` с реальными внешними процессами.

| Задача | Результат | Проверка |
|---|---|---|
| video | MP4, 360×640, AV1 + AAC, 5 927 153 байта | ffprobe: 162,702222 с; полное декодирование ffmpeg без ошибок |
| audio | OPUS, 2 300 866 байт | ffprobe: 162,680500 с; полное декодирование ffmpeg без ошибок |
| metadata | JSON, 1 107 218 байт | ID `N3vr7ir5BXc` совпадает с исходным видео |
| screenshot | Три JPEG: 0, 60, 120 секунд | Кадры непустые; кадры на 60 и 120 с просмотрены визуально |
| sub, ru | Markdown из автоматических русских VTT, 5 958 байт | Читаемый UTF-8, таймкод, текст и ключевые слова; нет строк VTT-таймингов и символов замены |

Авторских субтитров в этом видео нет. Проверялась техническая обработка автоматической
дорожки; точность распознавания речи вручную не проверялась. Качество 360×640 соответствует
доступному формату, выбранному текущим селектором с ограничением высоты 720 пикселей.

Результаты находятся в локальном каталоге `target/shorts-N3vr7ir5BXc/verified/`:
подкаталоги `video`, `audio`, `metadata`, `screenshot`, `sub`; логи каждой задачи,
`results.json` и `validation.json`. Файлы не включены в Git и удаляются при `mvn clean`.
`validation.json` содержит итоговые параметры и проверку очистки временных каталогов.
Для удобного открытия в корне `verified/` также сохранены копии `video.mp4`,
`audio.opus`, `subtitles.ru.md`, `metadata.json`, `frame-000.jpg`, `frame-060.jpg`, `frame-120.jpg`.

## Найденная ошибка и исправление

В первом прогоне кириллица повреждалась на границе Java/yt-dlp в Windows.
Из-за несовпадения кодировок видео, аудио и кадры возвращали ошибку поиска скачанного
файла, а имена метаданных и Markdown содержали символы замены.

Общий запуск yt-dlp теперь передаёт `--encoding utf-8`; `ProcessExecutor` явно читает UTF-8.
Добавлены два регрессионных теста: аргумент кодировки и реальное чтение Unicode-вывода
в дочерней JVM с `-Dfile.encoding=windows-1251`. Оба теста падали до исправления.

После правки: `mvn verify` — **119 тестов, 0 ошибок, падений и пропусков**.
Лог: `target/integration-fixes-verify.log`. Повторный реальный прогон всех пяти задач дал код `0`.

## Окружение и повторение

- Windows; Temurin JDK 21.0.12.1.
- yt-dlp 2026.08.19, FFmpeg/ffprobe 9.0.2, Node.js 24.20.0.
- yt-dlp и FFmpeg установлены через WinGet для пользователя, доступны в PATH.

После сборки и подготовки runtime-зависимостей по README:

```powershell
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://www.youtube.com/shorts/N3vr7ir5BXc" -t video -o output/integration/video
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://www.youtube.com/shorts/N3vr7ir5BXc" -t audio -o output/integration/audio
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://www.youtube.com/shorts/N3vr7ir5BXc" -t metadata -o output/integration/metadata
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://www.youtube.com/shorts/N3vr7ir5BXc" -t screenshot -i 60 -o output/integration/screenshot
java -cp "target/classes;target/dependency/*" com.ytdlpjava.cli.Main "https://www.youtube.com/shorts/N3vr7ir5BXc" -t sub -l ru -o output/integration/sub
```

Доступность видео, дорожек и форматов может измениться. Публичный YouTube-плейлист
по реальной ссылке и отправка Telegram пока не проверялись.

## Дополнительные сценарии

После успешной обработки Shorts выполнены проверки с установленными внешними инструментами:

| Сценарий | Результат |
|---|---|
| Недоступное видео `BaW_jenozKc`, задача metadata | CLI вернул `1` |
| Недоступный URL, затем Shorts пользователя | CLI вернул `1`, метаданные второго URL успешно сохранены |
| Две загрузки Shorts в одну папку | Оба запуска вернули `0`; созданы два MP4, SHA-256 первого файла не изменился |
| Прерывание реальной загрузки | Поток прерван после начала скачивания с `--limit-rate 1K`; CLI вернул `1`, interrupt-флаг сохранён, дочерние процессы завершены, временный каталог удалён |
| Контролируемый плейлист: недоступное видео и два вхождения Shorts | Сводка: 2 успешных, 1 неуспешный; CLI вернул `1`; два JSON сохранены в папке «Проверка плейлиста», коллизия получила суффикс ` (1)` |

Для контролируемого плейлиста только состав и название переданы через тестовый
JSON и `--load-info-json --no-clean-info-json`. Список URL и название разобрал реальный
yt-dlp, затем приложение обращалось к настоящим видео YouTube. Это не проверяет
доступ к публичному YouTube-плейлисту по его URL.

Тестовый стенд прерывания вызывает `Thread.interrupt()` внутри Java; обработка
нажатия Ctrl+C в отдельном терминале этим сценарием не проверяется.

Новых ошибок приложения не обнаружено; исходный код в этой серии проверок не менялся.
Локальные артефакты находятся в `target/boundary-checks/`: `results.json`,
`cancellation-result.json`, `playlist-result.json`, логи, метаданные и два MP4.
Предыдущие результаты в `target/shorts-N3vr7ir5BXc/verified/` сохранены.
