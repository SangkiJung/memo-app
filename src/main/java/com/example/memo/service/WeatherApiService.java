package com.example.memo.service;

import com.example.memo.config.WeatherApiProperties;
import com.example.memo.service.dto.WeatherApiCurrentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
public class WeatherApiService {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiService.class);

    private final RestTemplate restTemplate;
    private final WeatherApiProperties properties;

    public WeatherApiService(RestTemplate restTemplate, WeatherApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Realtime Weather API: 클라이언트 IP 또는 {@code auto:ip}로 위치·날씨를 한 번에 조회합니다.
     */
    public WeatherSnapshot fetchRealtimeForClientIp(String clientIp) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("WEATHER_API_KEY 미설정 — 날씨/위치 생략");
            return WeatherSnapshot.empty();
        }

        String q = toWeatherQuery(clientIp);
        try {
            URI uri = UriComponentsBuilder.fromUriString(trimTrailingSlash(properties.getBaseUrl()))
                    .pathSegment("current.json")
                    .queryParam("key", apiKey)
                    .queryParam("q", q)
                    .queryParam("lang", "ko")
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            WeatherApiCurrentResponse body = restTemplate.getForObject(uri, WeatherApiCurrentResponse.class);
            if (body == null || body.getLocation() == null || body.getCurrent() == null) {
                return WeatherSnapshot.empty();
            }

            String city = body.getLocation().getName();
            String conditionText = body.getCurrent().getCondition() != null
                    ? body.getCurrent().getCondition().getText()
                    : null;
            Double tempC = body.getCurrent().getTempC();

            if (city == null || city.isBlank()) {
                return WeatherSnapshot.empty();
            }
            return new WeatherSnapshot(city, conditionText, tempC);
        } catch (Exception e) {
            log.warn("WeatherAPI 호출 실패 (q={}): {}", q, e.getMessage());
            return WeatherSnapshot.empty();
        }
    }

    /**
     * 사설/루프백 IP는 WeatherAPI에 직접 넘기면 실패하는 경우가 많아 {@code auto:ip}로 대체합니다.
     */
    static String toWeatherQuery(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "auto:ip";
        }
        String ip = clientIp.trim();
        if ("127.0.0.1".equals(ip) || "::1".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)) {
            return "auto:ip";
        }
        if (ip.startsWith("10.")) {
            return "auto:ip";
        }
        if (ip.startsWith("192.168.")) {
            return "auto:ip";
        }
        if (ip.matches("172\\.(1[6-9]|2\\d|3[0-1])\\..+")) {
            return "auto:ip";
        }
        if (ip.toLowerCase().startsWith("fe80:")) {
            return "auto:ip";
        }
        return ip;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return "https://api.weatherapi.com/v1";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public record WeatherSnapshot(String location, String weatherCondition, Double tempC) {
        public static WeatherSnapshot empty() {
            return new WeatherSnapshot(null, null, null);
        }

        public boolean hasAny() {
            return location != null && !location.isBlank();
        }
    }
}
