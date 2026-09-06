package com.aldisued.iot.monitoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "alerts")
@Entity
public class Alert {

  public static final String SENSOR_ID_CONSTRAINT_NAME = "alerts_sensor_id";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String message;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sensor_id", nullable = false,
          foreignKey = @ForeignKey(name = SENSOR_ID_CONSTRAINT_NAME))
  private Sensor sensor;

  public Alert() {
  }

  public Alert(
          String message,
          LocalDateTime timestamp,
          Sensor sensor
  ) {
    this.message = message;
    this.timestamp = timestamp;
    this.sensor = sensor;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  // this is required to break the circular reference leading to infinite recursion at rendering to JSON
  @JsonIgnore
  public Sensor getSensor() {
    return sensor;
  }

  // this is required to show some information about the sensor now that it is not rendered to JSON
  @JsonProperty("sensorId")
  public UUID sensorId() {
    return sensor == null ? null : sensor.getId();
  }

  public void setSensor(Sensor sensor) {
    this.sensor = sensor;
  }
}
