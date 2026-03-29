package com.example.memo.repository;

import com.example.memo.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    List<ActionLog> findAllByOrderByTimestampDesc();
}
