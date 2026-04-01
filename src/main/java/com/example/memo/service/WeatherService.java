package com.example.memo.service;

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
            WeatherApiService.WeatherSnapshot snap;
            if (clientIp == null || clientIp.isBlank()) {
                log.error("클라이언트 IP를 인식하지 못했습니다. 기본 위치({})로 WeatherAPI 조회를 시도합니다.", FALLBACK_LOCATION);
                snap = weatherApiService.fetchRealtimeForQuery(FALLBACK_LOCATION);
            } else {
                snap = weatherApiService.fetchRealtimeForClientIp(clientIp);
            }
            log.info("현재 날씨 정보 조회 완료 - location={}, condition={}, tempC={}",
                    snap.location(), snap.weatherCondition(), snap.tempC());
            return snap;
        } catch (Exception e) {
            log.error("WeatherAPI 호출 중 예외 발생: {}", e.getMessage(), e);
            return WeatherApiService.WeatherSnapshot.empty();
        }
    }
}
