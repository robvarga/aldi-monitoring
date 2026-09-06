package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.service.SensorNameCollisionException;
import com.aldisued.iot.monitoring.service.SensorNotFoundException;
import com.aldisued.iot.monitoring.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sensor-readings")
public class SensorReadingController {

  private final SensorReadingService sensorReadingService;

  public SensorReadingController(SensorReadingService sensorReadingService) {
    this.sensorReadingService = sensorReadingService;
  }

  @PostMapping
  public SensorReading saveSensorReading(@Valid @RequestBody SensorReadingDto sensorReadingDto) {
    return sensorReadingService.saveSensorReading(sensorReadingDto);
  }

  @ExceptionHandler(SensorNotFoundException.class)
  ProblemDetail handleSensorNotFoundExceptionException(SensorNotFoundException e) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setTitle("Sensor not found: " + e.getSensorId());
    return problemDetail;
  }
}
