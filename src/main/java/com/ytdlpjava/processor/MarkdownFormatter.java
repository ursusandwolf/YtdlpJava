package com.ytdlpjava.processor;

import com.ytdlpjava.util.TextFormatUtils;
import java.util.List;

public class MarkdownFormatter {
    public String format(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            if (item.trim().isEmpty()) continue;

            if (item.startsWith("### [")) {
                if (sb.length() > 0) sb.append("\n\n");
                sb.append(item.trim());
                continue;
            }

            String paragraph = item;
            boolean shouldCapitalize = true;
            if (i > 0) {
                String prevParagraph = null;
                for (int j = i - 1; j >= 0; j--) {
                    if (!items.get(j).startsWith("### [")) {
                        prevParagraph = items.get(j);
                        break;
                    }
                }
                
                if (prevParagraph != null) {
                    if (!TextFormatUtils.isSentenceEnding(prevParagraph.charAt(prevParagraph.length() - 1))) {
                        shouldCapitalize = false;
                    }
                }
            }

            if (shouldCapitalize && paragraph.length() > 0) {
                paragraph = paragraph.substring(0, 1).toUpperCase() + paragraph.substring(1);
            } else if (paragraph.length() > 0) {
                paragraph = paragraph.substring(0, 1).toLowerCase() + paragraph.substring(1);
            }

            paragraph = splitLongSentences(paragraph, 300);
            boolean startsWithUpper = paragraph.length() > 0 && Character.isUpperCase(paragraph.charAt(0));
            paragraph = capitalizeAfterSplit(paragraph, startsWithUpper);

            if (sb.length() > 0) {
                if (sb.toString().trim().endsWith("]")) {
                    sb.append("\n");
                } else {
                    sb.append("\n\n");
                }
            }
            sb.append(wrapText(paragraph, 95));
        }
        return sb.toString().trim();
    }

    private String capitalizeAfterSplit(String text, boolean startsWithUpper) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = startsWithUpper;
        boolean firstLetterFound = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (nextUpper && Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
                firstLetterFound = true;
            } else if (!firstLetterFound && Character.isLetter(c)) {
                sb.append(c);
                firstLetterFound = true;
                nextUpper = false;
            } else {
                sb.append(c);
                if (TextFormatUtils.isSentenceEnding(c)) {
                    nextUpper = true;
                }
            }
        }
        return sb.toString();
    }

    private String splitLongSentences(String text, int maxLength) {
        String[] parts = text.split("(?<=[.!?…])\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > maxLength) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(forceSplitSentence(part, maxLength));
            } else {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private String forceSplitSentence(String sentence, int maxLength) {
        if (sentence.length() <= maxLength) return sentence;
        
        String searchArea = sentence.substring(0, Math.min(sentence.length(), maxLength));
        int breakPoint = searchArea.lastIndexOf(", ");
        if (breakPoint == -1) breakPoint = searchArea.lastIndexOf("; ");
        if (breakPoint == -1) breakPoint = searchArea.lastIndexOf(" "); 
        
        if (breakPoint != -1) {
            return sentence.substring(0, breakPoint + 1).trim() + ".\n" + 
                   Character.toUpperCase(sentence.charAt(breakPoint + 1)) + 
                   forceSplitSentence(sentence.substring(breakPoint + 2).trim(), maxLength);
        }
        
        return sentence;
    }

    private String wrapText(String text, int maxLength) {
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        int currentLineLength = 0;

        for (String word : words) {
            int wordLength = word.replaceAll("\\*\\*", "").length();
            if (currentLineLength + wordLength + 1 > maxLength) {
                result.append("\n");
                currentLineLength = 0;
            } else if (currentLineLength > 0) {
                result.append(" ");
                currentLineLength++;
            }
            result.append(word);
            currentLineLength += wordLength;
        }
        return result.toString();
    }
}
