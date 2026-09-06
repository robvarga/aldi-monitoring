package com.aldisued.iot.monitoring.service;

import java.util.UUID;

public class SensorNotFoundException extends ServiceFailedException {

  private final UUID sensorId;

  public SensorNotFoundException(Throwable cause, UUID sensorId) {
    this("Could not find sensor with id: "+sensorId, cause, sensorId);

  }

  public SensorNotFoundException(String message, Throwable cause, UUID sensorId) {
    super(message, cause);
    this.sensorId = sensorId;
  }

  public UUID getSensorId() {
    return sensorId;
  }
}
