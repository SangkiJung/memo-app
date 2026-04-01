package com.example.memo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class WeatherResolutionService {

    private static final Logger log = LoggerFactory.getLogger(WeatherResolutionService.class);
    private static final String FALLBACK_TEXT = "정보 없음";

    private final WeatherApiService weatherApiService;
    private final Executor weatherApiExecutor;

    public WeatherResolutionService(
            WeatherApiService weatherApiService,
            @Qualifier("weatherApiExecutor") Executor weatherApiExecutor) {
        this.weatherApiService = weatherApiService;
        this.weatherApiExecutor = weatherApiExecutor;
    }

    /**
     * 저장 요청 시에만 호출합니다. WeatherAPI 호출은 워커 스레드에서 수행하고, 완료될 때까지 대기한 뒤 스냅샷을 반환합니다.
     */
    public WeatherApiService.WeatherSnapshot resolveOnSave(boolean includeWeather, String clientIp) {
        if (!includeWeather) {
            return WeatherApiService.WeatherSnapshot.empty();
        }
        log.info("메모 저장 시 WeatherAPI 조회 시작 - clientIp={}", clientIp);
        try {
            WeatherApiService.WeatherSnapshot snap = CompletableFuture
                    .supplyAsync(() -> weatherApiService.fetchRealtimeForClientIp(clientIp), weatherApiExecutor)
                    .orTimeout(12, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.warn("WeatherAPI 비동기 조회 실패: {}", ex.getMessage());
                        return WeatherApiService.WeatherSnapshot.empty();
                    })
                    .join();
            if (!snap.hasAny()) {
                log.info("WeatherAPI 결과 비어있음 - 기본값으로 저장");
                return fallbackSnapshot();
            }
            return withFallbackFields(snap);
        } catch (Exception e) {
            log.warn("WeatherAPI 조회 중 오류: {}", e.getMessage());
            return fallbackSnapshot();
        }
    }

    private static WeatherApiService.WeatherSnapshot withFallbackFields(WeatherApiService.WeatherSnapshot snap) {
        String location = (snap.location() == null || snap.location().isBlank()) ? FALLBACK_TEXT : snap.location().trim();
        String condition = (snap.weatherCondition() == null || snap.weatherCondition().isBlank())
                ? FALLBACK_TEXT
                : snap.weatherCondition().trim();
        return new WeatherApiService.WeatherSnapshot(location, condition, snap.tempC());
    }

    private static WeatherApiService.WeatherSnapshot fallbackSnapshot() {
        return new WeatherApiService.WeatherSnapshot(FALLBACK_TEXT, FALLBACK_TEXT, null);
    }
}
