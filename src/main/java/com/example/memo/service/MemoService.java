package com.example.memo.service;

import com.example.memo.entity.ActionLog;
import com.example.memo.entity.ActionType;
import com.example.memo.entity.Memo;
import com.example.memo.repository.ActionLogRepository;
import com.example.memo.repository.MemoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MemoService {

    private final MemoRepository memoRepository;
    private final ActionLogRepository actionLogRepository;

    public MemoService(MemoRepository memoRepository, ActionLogRepository actionLogRepository) {
        this.memoRepository = memoRepository;
        this.actionLogRepository = actionLogRepository;
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
     * 위치·날씨는 호출자가 전달한 값으로만 저장합니다(저장 시점에 컨트롤러/서비스에서 WeatherAPI 조회 후 값이 채워집니다).
     */
    @Transactional
    public void createMemo(String title, String content, String cityName,
                           String weatherCondition, Double tempC) {
        Instant now = Instant.now();
        Memo memo = new Memo(
                title,
                content != null ? content : "",
                now,
                cityName,
                weatherCondition,
                tempC
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
