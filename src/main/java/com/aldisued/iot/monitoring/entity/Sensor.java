package com.aldisued.iot.monitoring.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table(name = "sensors", uniqueConstraints = @UniqueConstraint(name = Sensor.NAME_UNIQUE_CONSTRAINT_NAME, columnNames = "name"))
@Entity
public class Sensor {

  public static final String NAME_UNIQUE_CONSTRAINT_NAME = "sensors_name_key";

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SensorType type;

  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Alert> alerts = new ArrayList<>();

  @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SensorReading> sensorReadings = new ArrayList<>();

  public Sensor() {
  }

  public Sensor(String name, SensorType type) {
    this.name = name;
    this.type = type;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SensorType getType() {
    return type;
  }

  public void setType(SensorType type) {
    this.type = type;
  }

  /**
   * Note: this method fetches ALL alerts of this sensor, and it is a collection not bounded by requirements,
   * so can lead to OOM.
   * Having this collection here is really a bad idea, we should just store sensor id in the child objects
   * to decouple them from parent and still make it filterable.
   * Also, we handle this {@link List} with bag semantics, with element iteration order not retained.
   * If we tried to retain a stable list element order, it would require @{@link OrderColumn} or @{@link OrderBy}
   * (ideally with a new column for insertion DB timestamp to order by tie-broken by id) and that would incur
   * one or more of
   * <ul>
   *   <li>a potential performance penalty by having to fetch all existing readings</li>
   *   <li>break semantics upon concurrent inserts</li>
   *   <li>potentially break child insertion altogether</li>
   * </ul>
   *
   * @return a bag of alerts (list iteration order is not persisted
   * and is not guaranteed to be retained between sessions).
   */
  public List<Alert> getAlerts() {
    return alerts;
  }

  public void setAlerts(List<Alert> alerts) {
    if (alerts == this.alerts) {
      return;
    }
    this.alerts.clear();
    if (alerts != null) {
      alerts.forEach(r -> r.setSensor(this));
      this.alerts.addAll(alerts);
    }
  }

  /**
   * Note: this method fetches ALL sensor readings of this sensor, and it is a collection not bounded by requirements,
   * so can lead to OOM.
   * Having this collection here is really a bad idea, we should just store sensor id in the child objects
   * to decouple them from parent and still make it filterable.
   * Also, we handle this {@link List} with bag semantics, with element iteration order not retained.
   * If we tried to retain a stable list element order, it would require @{@link OrderColumn} or @{@link OrderBy}
   * (ideally with a new column for insertion DB timestamp to order by tie-broken by id) and that would incur
   * one or more of
   * <ul>
   *   <li>a potential performance penalty by having to fetch all existing readings</li>
   *   <li>break semantics upon concurrent inserts</li>
   *   <li>potentially break child insertion altogether</li>
   * </ul>
   *
   * @return a bag of sensor readings (list iteration order is not persisted
   * and is not guaranteed to be retained between sessions)
   */
  public List<SensorReading> getSensorReadings() {
    return sensorReadings;
  }

  public void setSensorReadings(List<SensorReading> sensorReadings) {
    if (sensorReadings == this.sensorReadings) {
      return;
    }
    this.sensorReadings.clear();                 // orphanRemoval deletes the old rows
    if (sensorReadings != null) {
      sensorReadings.forEach(r -> r.setSensor(this));   // child owns the FK
      this.sensorReadings.addAll(sensorReadings);
    }
  }

  @Override
  public boolean equals(Object o) {
    // entities should not override equals and hashCode (and should not act as keys)
    if (o == this) {
      return true;
    }
    // This is only correct because we did not subclass Sensor.
    // However, this makes it work correctly regardless of whether either o or this instance are a JPA proxy or not
    if (o instanceof Sensor sensor) {
      // calling getters to ensure that we are not comparing uninitialized values
      return Objects.equals(getId(), sensor.getId()) && Objects.equals(getName(), sensor.getName()) && getType() == sensor.getType();
    }
    return false;
  }

  @Override
  public int hashCode() {
    // entities should not override equals and hashCode (and should not act as keys)
    return Objects.hash(id, name, type);
  }
}
