package com.aldisued.iot.monitoring.service;

public class SensorNameCollisionException extends ServiceFailedException {

  private final String sensorName;

  public SensorNameCollisionException(Throwable cause, String sensorName) {
    this("Sensor name already exists: " + sensorName, cause, sensorName);
  }


  public SensorNameCollisionException(String message, Throwable cause, String sensorName) {
    super(message, cause);
    this.sensorName = sensorName;
  }

  public String getSensorName() {
    return sensorName;
  }
}
