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

    private String location;

    private String weatherDescription;

    private Double temperature;

    protected Memo() {
    }

    public Memo(String title, String content, Instant createdAt, String location,
                String weatherDescription, Double temperature) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.location = location;
        this.weatherDescription = weatherDescription;
        this.temperature = temperature;
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

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public Double getTemperature() {
        return temperature;
    }
}
