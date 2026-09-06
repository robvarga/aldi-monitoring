package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

  @Query("SELECT s.value FROM SensorReading s WHERE s.sensor.type = :sensorType AND s.timestamp BETWEEN :from AND :to ORDER BY s.timestamp ASC")
  List<Double> queryMeasurementValuesBySensorTypeBetween(SensorType sensorType, LocalDateTime from, LocalDateTime to);

  @Query("SELECT avg(s.value) FROM SensorReading s WHERE s.sensor.type = :sensorType AND s.timestamp BETWEEN :from AND :to")
  Optional<Double> queryAverageMeasurementValuesBySensorTypeBetween(SensorType sensorType, LocalDateTime from, LocalDateTime to);

}
