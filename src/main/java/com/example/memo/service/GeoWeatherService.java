package com.example.memo.service;

import com.example.memo.config.MemoExternalApiProperties;
import com.example.memo.service.dto.IpApiResponse;
import com.example.memo.service.dto.OpenWeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class GeoWeatherService {

    private static final Logger log = LoggerFactory.getLogger(GeoWeatherService.class);

    private final RestTemplate restTemplate;
    private final MemoExternalApiProperties properties;

    public GeoWeatherService(RestTemplate restTemplate, MemoExternalApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * IP로 도시를 조회하고, 가능하면 OpenWeatherMap으로 날씨·온도를 채웁니다.
     * 일부 단계가 실패해도 가능한 필드만 채운 결과를 반환합니다.
     */
    public WeatherSnapshot resolveFromClientIp(String clientIp) {
        String city = null;
        try {
            city = fetchCity(clientIp);
        } catch (Exception e) {
            log.warn("ip-api 호출 실패 (ip={}): {}", clientIp, e.getMessage());
        }
        if (city == null || city.isBlank()) {
            return WeatherSnapshot.empty();
        }

        String apiKey = properties.getOpenWeather().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("OPENWEATHER_API_KEY 미설정 — 도시만 저장: {}", city);
            return new WeatherSnapshot(city, null, null);
        }

        try {
            return fetchWeather(city);
        } catch (Exception e) {
            log.warn("OpenWeatherMap 호출 실패 (city={}): {}", city, e.getMessage());
            return new WeatherSnapshot(city, null, null);
        }
    }

    private String fetchCity(String clientIp) {
        URI uri = UriComponentsBuilder.fromUriString(trimTrailingSlash(properties.getIpApiBaseUrl()))
                .pathSegment(clientIp)
                .queryParam("fields", "status,message,city")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        IpApiResponse body = restTemplate.getForObject(uri, IpApiResponse.class);
        if (body == null || !"success".equalsIgnoreCase(body.getStatus())) {
            return null;
        }
        return body.getCity();
    }

    private WeatherSnapshot fetchWeather(String city) {
        URI uri = UriComponentsBuilder.fromUriString(trimTrailingSlash(properties.getOpenWeather().getBaseUrl()))
                .pathSegment("weather")
                .queryParam("q", city)
                .queryParam("appid", properties.getOpenWeather().getApiKey())
                .queryParam("units", "metric")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        OpenWeatherResponse body = restTemplate.getForObject(uri, OpenWeatherResponse.class);
        if (body == null) {
            return new WeatherSnapshot(city, null, null);
        }
        Double temp = body.getMain() != null ? body.getMain().getTemp() : null;
        String main = Optional.ofNullable(body.getWeather())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getMain())
                .orElse(null);
        return new WeatherSnapshot(city, main, temp);
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public record WeatherSnapshot(String city, String weatherMain, Double temperatureCelsius) {
        public static WeatherSnapshot empty() {
            return new WeatherSnapshot(null, null, null);
        }

        public boolean hasAny() {
            return city != null && !city.isBlank();
        }
    }
}
