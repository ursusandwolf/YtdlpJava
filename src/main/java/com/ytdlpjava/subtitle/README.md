# Пакет обработки субтитров

`com.ytdlpjava.subtitle` содержит обработку VTT внутри единого Maven-проекта.
Отдельного `pom.xml` и публикуемого артефакта нет.

| Компонент | Назначение |
|---|---|
| `api.SubtitleProcessor` | Сборка элементов текста из блоков |
| `model.SubtitleBlock` | Таймкод и текст сегмента |
| `config.SubtitleConfig` | Интервал таймкодов и пороги абзацев |
| `processor.SubtitleParser` | Разбор VTT |
| `processor.SubtitleLineCleaner` | Контракт очистки строк |
| `processor.impl.DefaultSubtitleLineCleaner` | Очистка сущностей, тегов и слов-паразитов |
| `processor.SubtitleTextAssembler` | Сборка абзацев и заголовков таймкодов |

Пакет не зависит от `model` и `processor` основного приложения, но использует
`com.ytdlpjava.util.TextFormatUtils`, Lombok и Apache Commons Text.
Для переноса нужны также утилита и зависимости.

`SubtitleConfig.defaultSettings()` задаёт `(10, 600, 800)`. CLI создаёт
`(180, 600, 800)` в `ApplicationFactory`. `paragraphHardLimit` ограничивает длину
абзацев на этапе сборки; при необходимости текст делится по пробелу или внутри
длинного слова без разрыва UTF-16 surrogate pair. Параметры проверяются при создании.
Markdown и ключевые слова добавляются после сборки элементов текста и могут
увеличить длину строки за счёт разметки.

См. [архитектуру проекта](../../../../../../docs/DOCUMENTATION.md).
