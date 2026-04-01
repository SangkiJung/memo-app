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
    private static final String FALLBACK_TEXT = "정보 없음";

    private final MemoRepository memoRepository;
    private final ActionLogRepository actionLogRepository;
    private final WeatherService weatherService;

    public MemoService(MemoRepository memoRepository, ActionLogRepository actionLogRepository, WeatherService weatherService) {
        this.memoRepository = memoRepository;
        this.actionLogRepository = actionLogRepository;
        this.weatherService = weatherService;
    }

    @Transactional(readOnly = true)
    public List<Memo> findAllMemos() {
        return memoRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * {@code query}가 비어 있으면 전체 목록, 아니면 제목 또는 내용에 부분 일치(대소문자 무시)하는 메모만 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<Memo> findMemosForList(String query) {
        if (query == null || query.isBlank()) {
            return memoRepository.findAllByOrderByCreatedAtDesc();
        }
        String q = query.trim();
        return memoRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(q, q);
    }

    @Transactional(readOnly = true)
    public List<ActionLog> findAllLogs() {
        return actionLogRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * 메모 저장 시점에 WeatherAPI를 호출해 위치·날씨를 채운 뒤 저장합니다.
     */
    @Transactional
    public void createMemo(String title, String content, boolean includeWeather, String clientIp) {
        Instant now = Instant.now();
        Memo memo = new Memo(title, content != null ? content : "", now, null, null, null);

        if (includeWeather) {
            WeatherApiService.WeatherSnapshot snap = weatherService.getWeather(clientIp);
            String location = (snap.location() == null || snap.location().isBlank()) ? FALLBACK_TEXT : snap.location().trim();
            String condition = (snap.weatherCondition() == null || snap.weatherCondition().isBlank()) ? FALLBACK_TEXT : snap.weatherCondition().trim();
            memo.setCityName(location);
            memo.setWeatherCondition(condition);
            memo.setTempC(snap.tempC());
        }

        log.info("저장될 메모 객체 상태: {}", memo);
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
