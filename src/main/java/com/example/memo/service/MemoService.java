package com.example.memo.service;

import com.example.memo.entity.ActionLog;
import com.example.memo.entity.ActionType;
import com.example.memo.entity.Memo;
import com.example.memo.repository.ActionLogRepository;
import com.example.memo.repository.MemoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MemoService {

    private static final Logger log = LoggerFactory.getLogger(MemoService.class);

    private final MemoRepository memoRepository;
    private final ActionLogRepository actionLogRepository;
    private final GeoWeatherService geoWeatherService;

    public MemoService(MemoRepository memoRepository, ActionLogRepository actionLogRepository,
                       GeoWeatherService geoWeatherService) {
        this.memoRepository = memoRepository;
        this.actionLogRepository = actionLogRepository;
        this.geoWeatherService = geoWeatherService;
    }

    @Transactional(readOnly = true)
    public List<Memo> findAllMemos() {
        return memoRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ActionLog> findAllLogs() {
        return actionLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public void createMemo(String title, String content, String clientIp) {
        Instant now = Instant.now();
        GeoWeatherService.WeatherSnapshot snap = GeoWeatherService.WeatherSnapshot.empty();
        try {
            snap = geoWeatherService.resolveFromClientIp(clientIp);
        } catch (Exception e) {
            log.warn("위치/날씨 조회 중 예외 — 메모는 저장합니다: {}", e.getMessage());
        }

        String location = snap.hasAny() ? snap.city() : null;
        String weatherDesc = snap.weatherMain();
        Double temp = snap.temperatureCelsius();

        Memo memo = new Memo(
                title,
                content != null ? content : "",
                now,
                location,
                weatherDesc,
                temp
        );
        memoRepository.save(memo);
        actionLogRepository.save(new ActionLog(ActionType.생성, title, now));
    }

    @Transactional
    public void deleteMemo(Long id) {
        Memo memo = memoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메모를 찾을 수 없습니다: " + id));
        String title = memo.getTitle();
        memoRepository.delete(memo);
        actionLogRepository.save(new ActionLog(ActionType.삭제, title, Instant.now()));
    }
}
