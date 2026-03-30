package com.example.memo.logging;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class P6SpySqlLogFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql == null || sql.isBlank()) {
            return "";
        }
        String oneLineSql = sql.replaceAll("\\s+", " ").trim();
        return "[SQL 로그] : " + oneLineSql;
    }
}
