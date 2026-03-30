package com.example.memo.entity;

import com.example.memo.support.WeatherDisplayHelper;
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

    /** 도시명 — DB 컬럼명은 {@code location} (예약어·Thymeleaf 혼동 방지를 위해 필드명만 cityName) */
    @Column(name = "location")
    private String cityName;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "temp_c")
    private Double tempC;

    protected Memo() {
    }

    public Memo(String title, String content, Instant createdAt, String cityName,
                String weatherCondition, Double tempC) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.cityName = cityName;
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

    public String getCityName() {
        return cityName;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public Double getTempC() {
        return tempC;
    }

    /** Thymeleaf에서 SpEL 빈({@code @weatherDisplay}) 없이 이모지 표시용 */
    public String getWeatherEmoji() {
        return WeatherDisplayHelper.emojiForConditionStatic(weatherCondition);
    }

    public boolean hasWeatherOrLocationInfo() {
        return (cityName != null && !cityName.isBlank())
                || (weatherCondition != null && !weatherCondition.isBlank())
                || tempC != null;
    }
}
