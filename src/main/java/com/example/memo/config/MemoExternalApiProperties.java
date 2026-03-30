package com.example.memo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "memo.external-api")
public class MemoExternalApiProperties {

    private String ipApiBaseUrl = "http://ip-api.com/json";

    private final OpenWeather openWeather = new OpenWeather();

    public String getIpApiBaseUrl() {
        return ipApiBaseUrl;
    }

    public void setIpApiBaseUrl(String ipApiBaseUrl) {
        this.ipApiBaseUrl = ipApiBaseUrl;
    }

    public OpenWeather getOpenWeather() {
        return openWeather;
    }

    public static class OpenWeather {

        private String apiKey = "";

        private String baseUrl = "https://api.openweathermap.org/data/2.5";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
