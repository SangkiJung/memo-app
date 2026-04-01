package com.example.memo.service;

import com.example.memo.config.WeatherApiProperties;
import com.example.memo.service.dto.WeatherApiCurrentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.annotation.PostConstruct;

import java.net.InetAddress;
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

    @PostConstruct
    void logConfigState() {
        String key = properties.getApiKey();
        boolean keyPresent = key != null && !key.isBlank();
        log.info("WeatherAPI 설정 - baseUrl={}, apiKeyConfigured={}",
                trimTrailingSlash(properties.getBaseUrl()), keyPresent);
        if (!keyPresent) {
            log.error("WEATHER_API_KEY가 비어 있습니다. 날씨/위치 정보는 기본값으로 저장됩니다.");
        }
    }

    /**
     * Realtime Weather API: 클라이언트 IP 또는 {@code auto:ip}로 위치·날씨를 한 번에 조회합니다.
     */
    public WeatherSnapshot fetchRealtimeForClientIp(String clientIp) {
        String apiKey = properties.getApiKey() != null ? properties.getApiKey().trim() : "";
        if (apiKey == null || apiKey.isBlank()) {
            log.error("WEATHER_API_KEY 미설정 - memo.weather-api.api-key 또는 WEATHER_API_KEY 환경변수를 확인하세요.");
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
            String requestUrlMasked = uri.toString().replace(apiKey, "****");
            log.info("WeatherAPI 호출 시작 - clientIp={}, query={}, url={}", clientIp, q, requestUrlMasked);

            WeatherApiCurrentResponse body = restTemplate.getForObject(uri, WeatherApiCurrentResponse.class);
            if (body == null || body.getLocation() == null || body.getCurrent() == null) {
                log.info("WeatherAPI 응답 비어있음/필드 누락 - query={}", q);
                return WeatherSnapshot.empty();
            }

            String city = body.getLocation().getName();
            String conditionText = body.getCurrent().getCondition() != null
                    ? body.getCurrent().getCondition().getText()
                    : null;
            Double tempC = body.getCurrent().getTempC();
            log.info("WeatherAPI 응답 수신 - city={}, condition={}, tempC={}", city, conditionText, tempC);

            if (city == null || city.isBlank()) {
                log.info("WeatherAPI 응답에 도시명이 없어 스냅샷을 비웁니다 - query={}", q);
                return WeatherSnapshot.empty();
            }
            return new WeatherSnapshot(city, conditionText, tempC);
        } catch (Exception e) {
            log.warn("WeatherAPI 호출 실패 (clientIp={}, q={}): {}", clientIp, q, e.getMessage());
            return WeatherSnapshot.empty();
        }
    }

    /**
     * WeatherAPI q 파라미터에 도시명/키워드를 직접 넘겨 조회합니다. (예: {@code Seoul})
     */
    public WeatherSnapshot fetchRealtimeForQuery(String qRaw) {
        String apiKey = properties.getApiKey() != null ? properties.getApiKey().trim() : "";
        if (apiKey == null || apiKey.isBlank()) {
            log.error("WEATHER_API_KEY 미설정 - memo.weather-api.api-key 또는 WEATHER_API_KEY 환경변수를 확인하세요.");
            return WeatherSnapshot.empty();
        }
        String q = (qRaw == null || qRaw.isBlank()) ? "Seoul" : qRaw.strip();
        try {
            URI uri = UriComponentsBuilder.fromUriString(trimTrailingSlash(properties.getBaseUrl()))
                    .pathSegment("current.json")
                    .queryParam("key", apiKey)
                    .queryParam("q", q)
                    .queryParam("lang", "ko")
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();
            String requestUrlMasked = uri.toString().replace(apiKey, "****");
            log.info("WeatherAPI 호출 시작(query 직접) - q={}, url={}", q, requestUrlMasked);

            WeatherApiCurrentResponse body = restTemplate.getForObject(uri, WeatherApiCurrentResponse.class);
            if (body == null || body.getLocation() == null || body.getCurrent() == null) {
                log.info("WeatherAPI 응답 비어있음/필드 누락 - q={}", q);
                return WeatherSnapshot.empty();
            }

            String city = body.getLocation().getName();
            String conditionText = body.getCurrent().getCondition() != null
                    ? body.getCurrent().getCondition().getText()
                    : null;
            Double tempC = body.getCurrent().getTempC();
            log.info("WeatherAPI 응답 수신(query 직접) - city={}, condition={}, tempC={}", city, conditionText, tempC);

            if (city == null || city.isBlank()) {
                log.info("WeatherAPI 응답에 도시명이 없어 스냅샷을 비웁니다 - q={}", q);
                return WeatherSnapshot.empty();
            }
            return new WeatherSnapshot(city, conditionText, tempC);
        } catch (Exception e) {
            log.error("WeatherAPI 호출 실패(query 직접) - q={}, reason={}", q, e.getMessage(), e);
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
        String ip = normalizeIp(clientIp.trim());
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()) {
                return "auto:ip";
            }
            return ip;
        } catch (Exception ignored) {
            return "auto:ip";
        }
    }

    private static String normalizeIp(String raw) {
        String ip = raw;
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring("::ffff:".length());
        }
        if (ip.startsWith("[") && ip.contains("]")) {
            ip = ip.substring(1, ip.indexOf(']'));
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
            return (location != null && !location.isBlank())
                    || (weatherCondition != null && !weatherCondition.isBlank())
                    || tempC != null;
        }
    }
}
