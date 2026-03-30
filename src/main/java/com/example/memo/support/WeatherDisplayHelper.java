package com.example.memo.support;

import org.springframework.stereotype.Component;

@Component("weatherDisplay")
public class WeatherDisplayHelper {

    public String emojiForCondition(String conditionText) {
        return emojiForConditionStatic(conditionText);
    }

    public static String emojiForConditionStatic(String conditionText) {
        if (conditionText == null || conditionText.isBlank()) {
            return "🌤️";
        }
        String t = conditionText.toLowerCase();
        if (containsAny(t, "thunder", "천둥", "번개")) {
            return "⛈️";
        }
        if (containsAny(t, "rain", "drizzle", "shower", "비", "이슬비", "소나기")) {
            return "🌧️";
        }
        if (containsAny(t, "snow", "sleet", "눈", "진눈깨비")) {
            return "❄️";
        }
        if (containsAny(t, "fog", "mist", "haze", "안개", "짙은 안개")) {
            return "🌫️";
        }
        if (containsAny(t, "cloud", "overcast", "구름", "흐림")) {
            return "☁️";
        }
        if (containsAny(t, "clear", "sunny", "맑", "화창")) {
            return "☀️";
        }
        return "🌤️";
    }

    private static boolean containsAny(String lower, String... needles) {
        for (String n : needles) {
            if (lower.contains(n)) {
                return true;
            }
        }
        return false;
    }
}
