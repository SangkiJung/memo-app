package com.example.memo.service;

import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final String FALLBACK_LOCATION = "Seoul";

    private final WeatherApiService weatherApiService;

    public WeatherService(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    /**
     * 저장 시점에만 호출됩니다.
     * IP를 못 찾으면 기본 위치로라도 조회를 시도합니다.
     */
    public WeatherApiService.WeatherSnapshot getWeather(String clientIp) {
        log.info("현재 날씨 정보를 가져오는 중... clientIp={}", clientIp);
        try {
            WeatherApiService.WeatherFetchResult result;
            if (clientIp == null || clientIp.isBlank()) {
                log.error("클라이언트 IP를 인식하지 못했습니다. 기본 위치({})로 WeatherAPI 조회를 시도합니다.", FALLBACK_LOCATION);
                result = weatherApiService.fetchRealtimeForQueryWithRaw(FALLBACK_LOCATION);
            } else {
                result = weatherApiService.fetchRealtimeForClientIpWithRaw(clientIp);
            }
            WeatherApiService.WeatherSnapshot snap = result.snapshot();
            log.info("현재 날씨 정보 조회 완료 - location={}, condition={}, tempC={}",
                    snap.location(), snap.weatherCondition(), snap.tempC());

            String city = snap.location();
            String condition = snap.weatherCondition();
            Double tempC = snap.tempC();
            String httpBody = result.rawBody();

            log.info("Datadog Custom Tags 추가 시도 - weather.city={}, weather.condition={}, weather.tempC={}",
                    city, condition, tempC);
            final Span span = GlobalTracer.get().activeSpan();
            if (span != null) {
                if (city != null) {
                    span.setTag("weather.city", city);
                }
                if (condition != null) {
                    span.setTag("weather.condition", condition);
                }
                if (tempC != null) {
                    span.setTag("weather.tempC", tempC);
                }
                if (httpBody != null) {
                    span.setTag("weather.httpbody", httpBody);
                }
                log.info("Datadog Custom Tags 추가 완료: weather.city={}, weather.condition={}, weather.tempC={}",
                        city, condition, tempC);
            } else {
                log.info("Datadog Custom Tags 추가 스킵: 활성 스팬이 없습니다.");
            }

            return snap;
        } catch (Exception e) {
            log.error("WeatherAPI 호출 중 예외 발생: {}", e.getMessage(), e);
            return WeatherApiService.WeatherSnapshot.empty();
        }
    }
}
