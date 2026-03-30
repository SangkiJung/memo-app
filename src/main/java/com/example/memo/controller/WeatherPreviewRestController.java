package com.example.memo.controller;

import com.example.memo.service.WeatherApiService;
import com.example.memo.support.ClientIpResolver;
import com.example.memo.support.WeatherDisplayHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherPreviewRestController {

    private final WeatherApiService weatherApiService;

    public WeatherPreviewRestController(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview(HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            WeatherApiService.WeatherSnapshot snap = weatherApiService.fetchRealtimeForClientIp(
                    ClientIpResolver.resolve(request));
            boolean hasData = snap.hasAny()
                    || snap.weatherCondition() != null
                    || snap.tempC() != null;
            if (!hasData) {
                body.put("ok", false);
                body.put("message", "위치·날씨를 가져오지 못했습니다. WEATHER_API_KEY와 네트워크를 확인하세요.");
                return ResponseEntity.ok(body);
            }
            body.put("ok", true);
            body.put("cityName", snap.hasAny() ? snap.location() : null);
            body.put("weatherCondition", snap.weatherCondition());
            body.put("tempC", snap.tempC());
            body.put("emoji", WeatherDisplayHelper.emojiForConditionStatic(snap.weatherCondition()));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("ok", false);
            body.put("message", "조회 중 오류가 발생했습니다.");
            return ResponseEntity.ok(body);
        }
    }
}
