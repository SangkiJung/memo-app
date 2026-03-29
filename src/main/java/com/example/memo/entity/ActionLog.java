package com.example.memo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "action_log")
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType actionType;

    @Column(nullable = false)
    private String memoTitle;

    @Column(nullable = false)
    private Instant timestamp;

    protected ActionLog() {
    }

    public ActionLog(ActionType actionType, String memoTitle, Instant timestamp) {
        this.actionType = actionType;
        this.memoTitle = memoTitle;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getMemoTitle() {
        return memoTitle;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
