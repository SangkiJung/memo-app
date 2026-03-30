package com.example.memo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "memo")
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** 도시명 (WeatherAPI location.name) */
    private String location;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "temp_c")
    private Double tempC;

    protected Memo() {
    }

    public Memo(String title, String content, Instant createdAt, String location,
                String weatherCondition, Double tempC) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.location = location;
        this.weatherCondition = weatherCondition;
        this.tempC = tempC;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getLocation() {
        return location;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public Double getTempC() {
        return tempC;
    }
}
