package com.aldisued.iot.monitoring.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record SensorReadingDto(
    @NotNull UUID sensorId,
    Double value,
    LocalDateTime timestamp
) {}
