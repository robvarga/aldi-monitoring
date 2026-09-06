package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.service.SensorNameCollisionException;
import com.aldisued.iot.monitoring.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sensors")
public class SensorController {

  private final SensorService sensorService;

  public SensorController(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  @PostMapping
  public Sensor saveSensor(@Valid @RequestBody SensorDto sensorDto) {
    return sensorService.saveSensor(sensorDto);
  }

  @ExceptionHandler(SensorNameCollisionException.class)
  ProblemDetail handleSensorNameCollisionException(SensorNameCollisionException e) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    problemDetail.setTitle("Sensor name already exists: " + e.getSensorName());
    return problemDetail;
  }
}