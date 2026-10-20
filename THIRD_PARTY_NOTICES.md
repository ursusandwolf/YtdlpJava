# Third-party components

The YtdlpJava source code is licensed under [MIT](LICENSE). The executable JAR
also contains third-party Java libraries, which retain their own licenses and notices.
The project license does not replace those licenses.

| Component | Version in v1.0.0 | License / upstream |
|---|---|---|
| SLF4J | 2.0.9 | [MIT](https://www.slf4j.org/license.html) |
| Logback Classic / Core | 1.4.11 | [EPL 1.0 / LGPL 2.1 dual license](https://logback.qos.ch/license.html) |
| JCommander | 1.82 | [Apache 2.0](https://github.com/cbeust/jcommander) |
| Apache Lucene | 9.9.2 | [Apache 2.0](https://lucene.apache.org/core/) |
| Apache Commons Text / Lang | 1.10.0 / 3.12.0 | [Apache 2.0](https://commons.apache.org/) |
| java-telegram-bot-api | 7.11.0 | [Apache 2.0](https://github.com/pengrad/java-telegram-bot-api) |
| Morfologik and Ukrainian dictionary | 2.1.9 / 4.9.1 | [BSD / Morfologik](https://github.com/morfologik/morfologik-stemming), [Apache 2.0 / Ukrainian dictionary](https://github.com/brown-uk/dict_uk) |
| OkHttp / Okio | 4.12.0 / 3.6.0 | [Apache 2.0](https://square.github.io/okhttp/) |
| Kotlin runtime | 1.9.10 / 1.9.22 | [Apache 2.0](https://github.com/JetBrains/kotlin) |
| Gson | 2.10.1 | [Apache 2.0](https://github.com/google/gson) |
| JetBrains annotations | 13.0 | [Apache 2.0](https://github.com/JetBrains/java-annotations) |

License and notice resources supplied by dependencies are preserved in `META-INF`
inside the executable JAR; common `LICENSE` / `NOTICE` resources are concatenated
during packaging. The application's license is stored separately as
`META-INF/YtdlpJava-LICENSE`. Some dependencies also carry notices outside `META-INF`.

Lombok is used only during compilation. JUnit and Mockito are test dependencies;
they are not part of the release JAR. The exact resolved dependency graph can be
inspected with `mvn dependency:tree -Dscope=runtime`.

FFmpeg, yt-dlp, Node.js and the Java runtime are installed separately and are not
bundled with the JAR. Their distribution terms are defined by their respective projects.
