> Архив подготовительного документа от 2026-10-11, до коммита и слияния.
> Указанные ниже ветки и невыполненные действия относятся к тому моменту.
> Слияние уже выполнено; актуальные сведения: [итоги слияния](../MERGE.md)
> и [состояние проекта](../PROJECT_CONTEXT.md). Исходный текст сохранён для истории;
> относительные ссылки обновлены для размещения в архиве.

# Подготовка dev → main

Документ подготовлен 2026-10-11. Изменения собраны локально в ветке `dev`;
коммит, push и слияние пока не выполнены.

## Проверенное состояние

- Удалённые ветки обновлены через `git fetch origin --prune`.
- `origin/dev`: `16f1c46`; `origin/main`: `0fb4454`.
- Локальные dev/main совпадают со своими удалёнными ветками до нового коммита.
- `origin/main` является предком dev: на момент проверки возможно fast-forward слияние.
- Все десять исправлений включены в подготовленный набор вместе с тестами и документацией.
- `mvn clean verify`: 58 тестов, 0 ошибок/падений/пропусков; BUILD SUCCESS.
- Smoke-проверки: справка обеих точек входа — 0, неверный интервал — 2.
- Результаты реальных YouTube/ffmpeg/Telegram интеграций не заявлены.
- Maven-версия остаётся `1.0-SNAPSHOT`; новый релиз и тег не создаются.

При изменении удалённых веток повторите проверку базы перед слиянием.
В main переносится всё приложение из dev: исходный main содержит только README.

## Текст коммита

```text
fix: harden media processing and prepare GitHub documentation

Enforce process timeouts, preserve playlist output directories and report
batch failures through CLI exit codes. Keep source files available to all
result handlers, reserve collision-free filenames atomically and check
Telegram API responses.

Restrict subtitle fallbacks to VTT, validate CLI settings, enforce paragraph
limits and close Lucene analyzers after each batch.

Add regression coverage and refresh README, UML, changelog and merge notes.
Validation: mvn clean verify (58 tests); CLI help and invalid-argument checks.
```

## Заголовок PR

```text
Prepare YtdlpJava for main: reliable processing, 58 tests and GitHub documentation
```

## Описание PR

В main добавляется Java 21 CLI для субтитров, аудио, видео, кадров и метаданных.
Перед переносом из dev исправлены десять проблем надёжности.

Зависшие процессы теперь ограничены таймаутом, результаты попадают в каталоги
плейлистов, а ошибки отдельных видео отражаются в итоговом статусе CLI.
Совпадающие имена сохраняются под разными именами; несколько обработчиков могут
читать один результат до очистки временного файла. Добавлены проверки Telegram API,
параметров CLI, VTT fallback, длины абзацев и закрытия анализаторов Lucene.

README содержит рабочий запуск через classpath, примеры и ограничения.
UML описывает контракты и последовательности; changelog объединён, старые документы
сохранены в архиве.

Проверки: `mvn clean verify` — 58 тестов, 0 ошибок/падений/пропусков.
Справка обеих точек входа возвращает 0, неверный интервал — 2.
Загрузка YouTube, обработка ffmpeg и отправка Telegram вживую не проверялись.
JAR пока запускается через classpath; Maven-версия остаётся 1.0-SNAPSHOT.

## Порядок коммита и публикации

1. Просмотреть индекс: `git diff --cached --stat`, `git diff --cached`,
   `git diff --cached --check`.
2. Убедиться, что автор настроен: `git config user.name`, `git config user.email`.
   Имя/email коммита и авторизация GitHub — разные настройки.
3. Создать коммит с текстом выше.
4. Выполнить обычный `git push origin dev`.
5. Открыть PR с base `main` и head `dev`, вставить описание выше.
6. Проверить актуальную базу, проверки GitHub и ограничения ветки; затем выполнить слияние.
7. Синхронизировать локальный main и проверить чистое состояние.

Для push достаточно Git с авторизацией. GitHub CLI необязателен;
для работы с PR через gh сначала выполните `gh auth login` и `gh auth status`.

## Если используется локальное fast-forward слияние

Только после коммита, просмотра результата и решения выполнить слияние:

```shell
git fetch origin --prune
git switch main
git merge --ff-only origin/main
git merge --ff-only dev
git push origin main
git switch dev
```

При ограничениях main используйте PR. Если `--ff-only` не проходит, сначала
разберите расхождение веток. Принудительный push для этого сценария не требуется.

## Перед выпуском

Определить лицензию, выпускную версию и формат распространения отдельно.
Настоящие ограничения перечислены в [README](../../README.md) и [анализе кода](../coderev.md).
