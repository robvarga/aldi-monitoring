package com.aldisued.iot.monitoring.tasks;

import static org.mockito.ArgumentMatchers.eq;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.service.AlertService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

@Sql(scripts = "/sql/task-5-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
public class Task6Tests extends IntegrationTestBase {

  private static final UUID SENSOR_ID = UUID.fromString(
      "e3242ea2-0514-46d3-aad8-b2012980c41c");

  private static final String TEST_TOPIC = AlertService.DEFAULT_TOPIC;

  @BeforeEach
  public void stubKafkaSend() {
    var record = new ProducerRecord<String, AlertDto>(TEST_TOPIC, null);
    var metadata = new RecordMetadata(
            new TopicPartition(TEST_TOPIC, 0), 0L, 0, System.currentTimeMillis(), 0, 0);
    Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any(AlertDto.class)))
            .thenReturn(CompletableFuture.completedFuture(new SendResult<>(record, metadata)));
  }

  @Autowired
  private AlertService alertService;

  @Autowired
  private AlertRepository alertRepository;

  @MockitoBean
  private KafkaTemplate<String, AlertDto> kafkaTemplate;

  @AfterEach
  public void cleanup() {
    alertRepository.deleteAll();
  }

  // this beg to be called verifyAlertProperties()
  @Test
  public void verifySensorReadingProperties() {
    var alertDto = testAlertDto();

    Alert alert = alertService.saveAlert(alertDto);

    Assertions.assertEquals(alertDto.message(), alert.getMessage());
    Assertions.assertEquals(alertDto.timestamp(), alert.getTimestamp());
  }

  @Test
  @Transactional
  public void verifySensorEntity() {
    var alertDto = testAlertDto();

    Alert alert = alertService.saveAlert(alertDto);

    Assertions.assertEquals(SENSOR_ID, alert.getSensor().getId());
  }

  @Test
  @Transactional
  public void verifyKafkaMessage() {
    var alertDto = testAlertDto();

    alertService.saveAlert(alertDto);

    Mockito.verify(
        kafkaTemplate,
        Mockito.times(1)).send(eq(TEST_TOPIC), eq(alertDto)
    );
  }

  private static @NotNull AlertDto testAlertDto() {
    return new AlertDto(
        SENSOR_ID,
        "Alert message",
        LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    );
  }

}
