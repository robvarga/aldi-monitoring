package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SensorReadingService {

  private final EntityManagerFactory emf;
  private final SensorReadingRepository sensorReadingRepository;
  private final SensorRepository sensorRepository;

  public SensorReadingService(EntityManagerFactory emf, SensorReadingRepository sensorReadingRepository, SensorRepository sensorRepository) {
    this.emf = emf;
    this.sensorReadingRepository = sensorReadingRepository;
    this.sensorRepository = sensorRepository;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public SensorReading saveSensorReading(SensorReadingDto sensorReadingDto) {
    UUID sensorId = sensorReadingDto.sensorId();
    Sensor sensor = sensorRepository.getReferenceById(sensorId);
    SensorReading sensorReading = new SensorReading(sensorReadingDto.value(), sensorReadingDto.timestamp(), sensor);
    try {
      sensorReading = sensorReadingRepository.saveAndFlush(sensorReading);
    } catch (RuntimeException e) {
      Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
      String message = cause.getMessage();
      if (message != null && message.contains(SensorReading.SENSOR_ID_CONSTRAINT_NAME)) {
        throw new SensorNotFoundException("Could not save sensor reading, referenced sensor with id " + sensorId + " not found", cause, sensorId);
      }
      throw e;
    }
    if (emf.getPersistenceUnitUtil().isLoaded(sensor, "sensorReadings")) {
      sensor.getSensorReadings().add(sensorReading);
    }
    return sensorReading;
  }

}
