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
        try {
            return CompletableFuture
                    .supplyAsync(() -> weatherApiService.fetchRealtimeForClientIp(clientIp), weatherApiExecutor)
                    .orTimeout(12, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.warn("WeatherAPI 비동기 조회 실패: {}", ex.getMessage());
                        return WeatherApiService.WeatherSnapshot.empty();
                    })
                    .join();
        } catch (Exception e) {
            log.warn("WeatherAPI 조회 중 오류: {}", e.getMessage());
            return WeatherApiService.WeatherSnapshot.empty();
        }
    }
}
