package com.example.memo.support;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].strip();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.strip();
        }
        String addr = request.getRemoteAddr();
        return normalizeIp(addr != null ? addr : "");
    }

    private static String normalizeIp(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.strip();
        if (value.isEmpty()) {
            return "";
        }
        if (value.startsWith("::ffff:")) {
            value = value.substring("::ffff:".length());
        }
        if (value.startsWith("[") && value.contains("]")) {
            value = value.substring(1, value.indexOf(']'));
        } else if (value.indexOf(':') >= 0 && value.chars().filter(ch -> ch == ':').count() == 1) {
            value = value.substring(0, value.indexOf(':'));
        }
        return value;
    }
}
