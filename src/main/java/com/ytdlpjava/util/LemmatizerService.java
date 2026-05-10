package com.ytdlpjava.util;

public class LemmatizerService {

    public enum Language { RU, EN }

    public String getLemma(String word, Language lang) {
        if (lang == Language.RU) {
            return RussianStemmer.stem(word);
        }
        // Simplified English stemming
        String stem = word.toLowerCase();
        if (stem.endsWith("ing") && stem.length() > 5) stem = stem.substring(0, stem.length() - 3);
        else if (stem.endsWith("ed") && stem.length() > 4) stem = stem.substring(0, stem.length() - 2);
        return stem;
    }
}
