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

    @Transactional(readOnly = true)
    public List<ActionLog> findAllLogs() {
        return actionLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public void createMemo(String title, String content) {
        Instant now = Instant.now();
        Memo memo = new Memo(title, content != null ? content : "", now);
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
