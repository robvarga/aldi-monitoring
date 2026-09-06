package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.service.AlertService;
import com.aldisued.iot.monitoring.service.SensorNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/alerts")
public class AlertController {

  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @PostMapping
  public Alert saveAlert(@Valid @RequestBody AlertDto alertDto) {
    return alertService.saveAlert(alertDto);
  }

  @GetMapping("/latest")
  public ResponseEntity<AlertDto> getLatestAlert(@RequestParam UUID sensorId) {
    return ResponseEntity.of(alertService.findLastAlertBySensorId(sensorId));
  }

  @ExceptionHandler(SensorNotFoundException.class)
  ProblemDetail handleSensorNotFoundExceptionException(SensorNotFoundException e) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setTitle("Sensor not found: " + e.getSensorId());
    return problemDetail;
  }
}
