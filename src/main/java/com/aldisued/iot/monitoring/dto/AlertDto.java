package com.aldisued.iot.monitoring.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertDto(
    @NotNull UUID sensorId,
    String message,
    LocalDateTime timestamp
) {
}
