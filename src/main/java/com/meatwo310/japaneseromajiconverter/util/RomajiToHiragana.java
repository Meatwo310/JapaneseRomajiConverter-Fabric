package com.meatwo310.japaneseromajiconverter.util;

import com.meatwo310.japaneseromajiconverter.JapaneseRomajiConverter;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class RomajiToHiragana {
    private static final LinkedHashMap<String, String[]> romajiToJapaneseMap = new LinkedHashMap<>();

    private static void setupRomajiMapFromResourceName(String resourceName) {
        URL pathToTable = RomajiToHiragana.class.getResource(resourceName);

        if (pathToTable == null) {
            System.out.println("Could not find resource: " + resourceName);
            return;
        }

        try (java.io.InputStream stream = pathToTable.openStream()) {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String[] body = new String[2];
                switch (parts.length) {
                    case 2 -> {
                        body[0] = parts[1];
                        body[1] = "0";
                    }
                    case 3 -> {
                        // Int型に変換可能か確認
                        try {
                            Integer.parseInt(parts[2]);
                        } catch (Exception e) {
                            JapaneseRomajiConverter.LOGGER.warn("Invalid line in " + resourceName + "; This line will be ignored: " + line);
                            continue;
                        }
                        body[0] = parts[1];
                        body[1] = parts[2];
                    }
                    default -> {
                        // throw new RuntimeException("Invalid line in romaji_to_hiragana.csv: " + line);
                        if (!line.matches("")) {
                            JapaneseRomajiConverter.LOGGER.warn("Invalid line in " + resourceName + "; This line will be ignored: " + line);
                        }
                        continue;
                    }
                }

                romajiToJapaneseMap.put(parts[0], body);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        // setupRomajiMapFromResourceName("/assets/japaneseromajiconverter/romaji_to_hiragana_0.csv");
        setupRomajiMapFromResourceName("/assets/japaneseromajiconverter/romaji_to_hiragana.csv");
        setupRomajiMapFromResourceName("/assets/japaneseromajiconverter/romaji_to_hiragana_2.csv");
    }

    public static String convertRomajiToHiragana(String romaji) {
        romaji = romaji.toLowerCase();
        StringBuilder result = new StringBuilder();
        final int length = romaji.length();
        boolean found;
//        int i = 0;
//        while (i < romaji.length()) {
        for (int i = 0; i < length;) {
            found = false;
            for (int j = 4; j >= 1; j--) {
                if (i + j <= romaji.length()) {
                    String substring = romaji.substring(i, i + j);
                    if (romajiToJapaneseMap.containsKey(substring)) {
                        result.append(romajiToJapaneseMap.get(substring)[0]);
                        i += j + Integer.parseInt(romajiToJapaneseMap.get(substring)[1]);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                result.append(romaji.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    public static String convertMessageStringToHiragana(String message) {
        String resultText;
        final String convertedText = RomajiToHiragana.convertRomajiToHiragana(message);

        // 空文字は変換しない
        if (message.isEmpty()) {
            resultText = message;
        } else if (convertedText.equals(message)) {
            // 変換前後で文字列が変わらない場合は変換しない
            resultText = message;
        } else if (message.matches("^[!#;].*$")) {
            // !#;で始まる場合は変換しないが、頭文字はグレーに変換する
            resultText = "§7" + message.charAt(0) + "§r" + message.substring(1);
        } else if (message.matches("^[\\\\¥￥].*$")) {
            // \¥￥で始まる場合は頭文字を取り除いて強制的に変換し、括弧付きで原文を示す
            resultText = RomajiToHiragana.convertRomajiToHiragana(message.substring(1)) + " §7(" + message + ")";
        } else if (message.length() < 5) {
            // 短すぎる場合は変換しない
            resultText = message;
        } else if (message.startsWith(":")) {
            // :で始まる場合は変換しない
            resultText = message;
        } else if (!message.matches("^[!-~\\s¥￥]+$")) {
            // ASCII文字のみではない場合は変換しない
            resultText = message;
        } else if (convertedText.replaceAll("[^a-zA-Z]", "").length() > convertedText.length() * 0.25) {
            // 変換結果にアルファベットが全体の25%よりも多く含まれる場合は変換を失敗させる
            resultText = message + " §8(変換失敗)";
        } else {
            // 条件に当てはまらない普通のローマ字メッセージは変換する
            resultText = convertedText + " §7(" + message + ")";
        }

        return resultText;
    }
}
