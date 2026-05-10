package com.ytdlpjava.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RussianStemmer {
    // Porter Stemmer for Russian
    private static final Pattern RV = Pattern.compile("^(.*?[аеиоуыэюя])(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERFECTIVE_GERUND = Pattern.compile("(ив|ивши|ившись|ыв|ывши|ывшись|я|а)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ADJECTIVE = Pattern.compile("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ое|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ую)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PARTICIPLE = Pattern.compile("(ивш|ывш|ующ|ем|нн|вш|ющ|щ|т)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern REFLEXIVE = Pattern.compile("(ся|сь)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VERB = Pattern.compile("(ла|на|ете|йте|ли|й|л|ем|н|ло|ет|ют|ны|ть|ешь|нно)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOUN = Pattern.compile("(а|ев|ов|ие|ье|е|иями|ями|ами|ией|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я)$", Pattern.CASE_INSENSITIVE);

    public static String stem(String word) {
        if (word == null || word.length() < 3) return word;
        String lower = word.toLowerCase();
        
        Matcher rvMatcher = RV.matcher(lower);
        if (!rvMatcher.matches()) return lower;
        
        String rv = rvMatcher.group(2);
        
        rv = REFLEXIVE.matcher(rv).replaceFirst("");
        
        String temp = PERFECTIVE_GERUND.matcher(rv).replaceFirst("");
        if (!temp.equals(rv)) {
            rv = temp;
        } else {
            temp = ADJECTIVE.matcher(rv).replaceFirst("");
            if (!temp.equals(rv)) {
                rv = temp;
            } else {
                temp = PARTICIPLE.matcher(rv).replaceFirst("");
                if (!temp.equals(rv)) {
                    rv = temp;
                }
                temp = VERB.matcher(rv).replaceFirst("");
                if (!temp.equals(rv)) {
                    rv = temp;
                }
            }
        }
        
        rv = NOUN.matcher(rv).replaceFirst("");
        
        return rvMatcher.group(1) + rv;
    }
}
