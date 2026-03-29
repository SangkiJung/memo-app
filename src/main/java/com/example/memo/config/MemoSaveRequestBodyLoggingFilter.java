package com.example.memo.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MemoSaveRequestBodyLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MemoSaveRequestBodyLoggingFilter.class);

    private final ObjectMapper objectMapper;

    public MemoSaveRequestBodyLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isMemoCreatePost(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
        filterChain.doFilter(wrapped, response);

        byte[] cached = wrapped.getContentAsByteArray();
        if (cached.length == 0) {
            return;
        }

        String body = new String(cached, StandardCharsets.UTF_8);
        String contentType = wrapped.getContentType();
        String formatted = formatBodyForLog(body, contentType);
        log.info("[입력 데이터 로그] : {}", formatted);
    }

    private boolean isMemoCreatePost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        return "/memos".equals(path);
    }

    private String formatBodyForLog(String body, String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            try {
                JsonNode node = objectMapper.readTree(body);
                return jsonNodeToBraceString(node);
            } catch (Exception e) {
                return body;
            }
        }
        if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            return formUrlEncodedToBraceString(body);
        }
        return body;
    }

    private String jsonNodeToBraceString(JsonNode node) {
        if (node == null || node.isNull()) {
            return "{}";
        }
        if (!node.isObject()) {
            return node.toString();
        }
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        node.fields().forEachRemaining(entry -> {
            JsonNode v = entry.getValue();
            String valueStr = v.isTextual() ? "\"" + escapeForLog(v.asText()) + "\"" : v.toString();
            joiner.add(entry.getKey() + ": " + valueStr);
        });
        return joiner.toString();
    }

    private String formUrlEncodedToBraceString(String body) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key;
            String rawValue;
            if (eq < 0) {
                key = urlDecode(pair);
                rawValue = "";
            } else {
                key = urlDecode(pair.substring(0, eq));
                rawValue = urlDecode(pair.substring(eq + 1));
            }
            if ("_csrf".equals(key)) {
                continue;
            }
            map.put(key, rawValue);
        }
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (Map.Entry<String, String> e : map.entrySet()) {
            joiner.add(e.getKey() + ": \"" + escapeForLog(e.getValue()) + "\"");
        }
        return joiner.toString();
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private static String escapeForLog(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
