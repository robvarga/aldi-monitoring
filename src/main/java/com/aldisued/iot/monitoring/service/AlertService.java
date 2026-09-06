package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AlertService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);

  public static final String DEFAULT_TOPIC = "alerts";
  private static final boolean DEFAULT_WAIT_FOR_KAFKA_BROKER = false;


  private final EntityManagerFactory emf;
  private final AlertRepository alertRepository;
  private final SensorRepository sensorRepository;
  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  private String topic = DEFAULT_TOPIC;
  private boolean waitForKafkaBroker = DEFAULT_WAIT_FOR_KAFKA_BROKER;

  public AlertService(EntityManagerFactory emf, AlertRepository alertRepository, SensorRepository sensorRepository, KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.emf = emf;
    this.alertRepository = alertRepository;
    this.sensorRepository = sensorRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public Alert saveAlert(AlertDto alertDto) {
    UUID sensorId = alertDto.sensorId();
    Sensor sensor = sensorRepository.getReferenceById(sensorId);
    Alert alert = new Alert(alertDto.message(), alertDto.timestamp(), sensor);
    try {
      alert = alertRepository.saveAndFlush(alert);
    } catch (final RuntimeException e) {
      Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
      String message = cause.getMessage();
      if (message != null && message.contains(Alert.SENSOR_ID_CONSTRAINT_NAME)) {
        throw new SensorNotFoundException("Could not save alert, referenced sensor with id " + sensorId + " not found", cause, sensorId);
      }
      throw e;
    }
    if (emf.getPersistenceUnitUtil().isLoaded(sensor, "alerts")) {
      sensor.getAlerts().add(alert);
    }

    // This should actually be done in a separate service to satisfy the Single Responsibility Principle
    // (provided it was not even referenced from this service, but called from controller or a composing service)
    // Also this should be done after the commit either from an after-commit hook or called separately from the
    // controller, because:
    // 1. Kafka send can take a noticeable amount of time and you should not have to wait with committing
    //    the DB transaction until Kafka sending is confirmed
    // 2. you should not lose the alert from the DB even if sending to Kafka fails and also
    trySendToKafkaSwallowException(alertDto);

    return alert;
  }

  void trySendToKafkaSwallowException(AlertDto alertDto) {
    try {
      CompletableFuture<SendResult<String, AlertDto>> future = kafkaTemplate.send(topic, alertDto);
      final boolean traceEnabled = LOGGER.isTraceEnabled();
      final boolean waitForKafkaBroker = this.waitForKafkaBroker;
      if (traceEnabled || !waitForKafkaBroker) {
        future.whenComplete((result, ex) -> {
          if (ex != null) {
            if (!waitForKafkaBroker) {
              LOGGER.error("Failed to send alert to Kafka", ex);
            }
          } else if (traceEnabled) {
            LOGGER.trace("Sent alert from sensor {} to Kafka topic {} at offset {}", alertDto.sensorId(), topic, result.getRecordMetadata().offset());
          }
        });
      }
      if (waitForKafkaBroker) {
        future.get();
      }
    } catch (ExecutionException e) {
      LOGGER.error("Failed to send alert to Kafka", e.getCause());
    } catch (InterruptedException e) {
      LOGGER.warn("Interrupted while sending alert to Kafka", e);
      Thread.currentThread().interrupt();
    } catch (RuntimeException e) {
      LOGGER.error("Failed to send alert to Kafka", e);
    }
  }

  public Optional<AlertDto> findLastAlertBySensorId(@NotNull UUID sensorId) {
    return alertRepository.findLatestAlertBySensorId(sensorId);
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public void setWaitForKafkaBroker(boolean waitForKafkaBroker) {
    this.waitForKafkaBroker = waitForKafkaBroker;
  }
}
