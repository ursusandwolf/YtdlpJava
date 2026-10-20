# Выпуск YtdlpJava

## Что входит в релиз

Готовый исполняемый JAR, контрольная сумма SHA-256, Git-тег и описание GitHub Release.
Бинарные файлы внешних инструментов и скачанные с YouTube материалы не публикуются.

## Подготовка

1. Обновить версию в `pom.xml`, имена артефактов в `.github/workflows/ci.yml`,
   README и примерах. Обновить changelog и документ `docs/releases/vVERSION.md`.
2. Выполнить `mvn clean verify`: Surefire проверяет код, Shade собирает JAR,
   Failsafe запускает три проверки готового артефакта.
3. Проверить нужные реальные сценарии с установленными yt-dlp/FFmpeg/Node.js.
   Зафиксировать результат и ограничения в `docs/INTEGRATION_CHECKS.md`.
4. Создать коммит и отправить его в `main`. Дождаться успеха обоих заданий
   workflow `Build and test`: Windows и Linux.

`mvn clean` удаляет `target`, включая локально скачанные тестовые материалы.
При необходимости сохраните их заранее либо используйте `mvn verify` для локального
прогона. CI всегда собирает чистую рабочую копию.

## Публикация v1.0.0

Публикуйте артефакт из успешного Linux-задания CI, а не случайный локальный файл.
Workflow сохраняет JAR и `SHA256SUMS` в артефакте `ytdlp-java-1.0.0`.

```shell
gh run list --workflow ci.yml --branch main
gh run download RUN_ID --name ytdlp-java-1.0.0 --dir target/release-assets
```

Сверьте SHA-256 и убедитесь, что оба задания этого запуска зелёные.
Создайте аннотированный тег на том же проверенном коммите:

```shell
git tag -a v1.0.0 COMMIT_SHA -m "YtdlpJava 1.0.0"
git push origin v1.0.0
gh release create v1.0.0 target/release-assets/ytdlp-java-1.0.0.jar target/release-assets/SHA256SUMS --verify-tag --title "YtdlpJava 1.0.0 — готовый JAR и пять задач для YouTube" --notes-file docs/releases/v1.0.0.md
```

## После публикации

- Проверить ссылку релиза, имя тега и коммит.
- Скачать опубликованный JAR, проверить SHA-256 и запустить `java -jar ... --help`.
- Проверить наличие обоих Assets и корректность ссылок из описания.
- Убедиться, что push тега также проходит CI.

Опубликованные теги не перемещать и артефакты под той же версией не заменять.
Исправления выпускать следующей версией. Известные ограничения описывать в release notes.
