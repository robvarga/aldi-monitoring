package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

  private final SensorRepository sensorRepository;

  public SensorService(SensorRepository sensorRepository) {
    this.sensorRepository = sensorRepository;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public Sensor saveSensor(SensorDto sensor) {
    try {
      return sensorRepository.saveAndFlush(new Sensor(
              sensor.name(),
              sensor.type()
      ));
    } catch (final RuntimeException e) {
      Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
      String message = cause.getMessage();
      if (message != null && message.contains(Sensor.NAME_UNIQUE_CONSTRAINT_NAME)) {
        throw new SensorNameCollisionException(e, sensor.name());
      }
      throw e;
    }
  }
}
