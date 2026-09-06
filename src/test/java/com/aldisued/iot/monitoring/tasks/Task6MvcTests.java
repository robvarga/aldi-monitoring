package com.aldisued.iot.monitoring.tasks;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.service.AlertService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/sql/task-5-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class Task6MvcTests extends IntegrationTestBase {

  private static final UUID SENSOR_ID = UUID.fromString(
          "e3242ea2-0514-46d3-aad8-b2012980c41c");
  private static final String TEST_TOPIC = AlertService.DEFAULT_TOPIC;
  private static final String BASE_ENDPOINT = "/alerts";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AlertRepository alertRepository;

  @MockitoBean
  private KafkaTemplate<String, AlertDto> kafkaTemplate;

  @BeforeEach
  public void stubKafkaSend() {
    var record = new ProducerRecord<String, AlertDto>(TEST_TOPIC, null);
    var metadata = new RecordMetadata(
            new TopicPartition(TEST_TOPIC, 0), 0L, 0, System.currentTimeMillis(), 0, 0);
    Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any(AlertDto.class)))
            .thenReturn(CompletableFuture.completedFuture(new SendResult<>(record, metadata)));
  }

  @AfterEach
  public void cleanup() {
    alertRepository.deleteAll();
  }

  @Test
  public void verifyAlertProperties() throws Exception {
    var alertDto = testAlertDto();

    Alert alert = postAndGetAlert(alertDto, StatusResultMatchers::isOk);
    Assertions.assertNotNull(alert);
    Assertions.assertEquals(alertDto.message(), alert.getMessage());
    Assertions.assertEquals(alertDto.timestamp(), alert.getTimestamp());
  }

  @Test
  @Transactional
  public void verifyAlert() throws Exception {
    var alertDto = testAlertDto();

    Alert alert = postAndGetAlert(alertDto, StatusResultMatchers::isOk);
    Assertions.assertNotNull(alert);
    Assertions.assertEquals(SENSOR_ID, alert.sensorId());
  }

  @Test
  public void verifyAbsentSensorResultsInBadRequest() throws Exception {
    var alertDto = new AlertDto(
            UUID.randomUUID(),
            "Alert message",
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    );

    postAndGetAlert(alertDto, StatusResultMatchers::isBadRequest);
  }

  private Alert postAndGetAlert(AlertDto alertDto,
                                Function<StatusResultMatchers, ResultMatcher> statusTester) throws Exception {

    MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post(BASE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(alertDto)))
            .andExpect(statusTester.apply(status()))
            .andReturn();

    MockHttpServletResponse response = res.getResponse();
    if (response.getStatus() == HttpStatus.OK.value()) {
      JsonNode body = objectMapper.readTree(response.getContentAsString());
      Assertions.assertAll(
              () -> Assertions.assertEquals(SENSOR_ID.toString(), body.path("sensorId").asText()),
              () -> Assertions.assertTrue(body.path("sensor").isMissingNode(), "sensor must not be serialized"),
              () -> Assertions.assertEquals(alertDto.timestamp(), LocalDateTime.parse(body.path("timestamp").asText())),
              () -> Assertions.assertEquals(alertDto.message(), body.path("message").asText()));
      return alertRepository.findById(body.path("id").asLong()).orElseThrow();
    }
    return null;
  }

  private static AlertDto testAlertDto() {
    return new AlertDto(
            SENSOR_ID,
            "Alert message",
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    );
  }
}
