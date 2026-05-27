package com.example.smartinspection.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SyncDateConvertService {
    private static final LocalDateTime MIN_VALID = LocalDateTime.of(1900, 1, 1, 0, 0);

    public LocalDateTime toLocalDateTime(Timestamp value) {
        return normalize(value);
    }

    public LocalDateTime normalize(Timestamp value) {
        if (value == null) return null;
        LocalDateTime t = value.toLocalDateTime();
        return t.isBefore(MIN_VALID) ? null : t;
    }
}
