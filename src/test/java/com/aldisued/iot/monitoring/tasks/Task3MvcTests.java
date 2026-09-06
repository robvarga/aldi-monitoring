package com.aldisued.iot.monitoring.tasks;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Function;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/sql/task-3-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
public class Task3MvcTests extends IntegrationTestBase {

  private static final UUID SENSOR_ID = UUID.fromString(
          "e3242ea2-0514-46d3-aad8-b2012980c41c");
  private static final String BASE_ENDPOINT = "/sensor-readings";

  @Autowired
  private SensorReadingRepository sensorReadingRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @AfterEach
  public void cleanup() {
    sensorReadingRepository.deleteAll();
  }

  @Test
  public void verifySensorReadingProperties() throws Exception {
    var sensorReadingDto = testSensorReadingDto();

    SensorReading sensorReadingEntity = postAndGetSensorReading(sensorReadingDto, StatusResultMatchers::isOk);

    Assertions.assertEquals(sensorReadingDto.value(), sensorReadingEntity.getValue());
    Assertions.assertEquals(sensorReadingDto.timestamp(), sensorReadingEntity.getTimestamp());
  }

  @Test
  @Transactional
  public void verifySensorEntity() throws Exception {
    var sensorReadingDto = testSensorReadingDto();

    SensorReading sensorReadingEntity = postAndGetSensorReading(sensorReadingDto, StatusResultMatchers::isOk);

    Assertions.assertEquals(SENSOR_ID, sensorReadingEntity.getSensor().getId());
  }

  @Test
  public void verifyAbsentSensorResultsInBadRequest() throws Exception {
    var sensorReadingDto = new SensorReadingDto(
            UUID.randomUUID(),
            23.45,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    );

    postAndGetSensorReading(sensorReadingDto, StatusResultMatchers::isBadRequest);
  }

  private SensorReading postAndGetSensorReading(SensorReadingDto sensorReadingDto,
                                                Function<StatusResultMatchers, ResultMatcher> statusTester) throws Exception {
    MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post(BASE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sensorReadingDto)))
            .andExpect(statusTester.apply(status()))
            .andReturn();

    MockHttpServletResponse response = res.getResponse();
    if (response.getStatus() == HttpStatus.OK.value()) {
      JsonNode body = objectMapper.readTree(response.getContentAsString());
      Assertions.assertAll(
              () -> Assertions.assertEquals(SENSOR_ID.toString(), body.path("sensorId").asText()),
              () -> Assertions.assertTrue(body.path("sensor").isMissingNode(), "sensor must not be serialized"),
              () -> Assertions.assertEquals(sensorReadingDto.timestamp(), LocalDateTime.parse(body.path("timestamp").asText())),
              () -> Assertions.assertEquals(sensorReadingDto.value(), body.path("value").asDouble(), 0.0001));
      return sensorReadingRepository.findById(body.path("id").asLong()).orElseThrow();
    }
    return null;
  }

  private static @NotNull SensorReadingDto testSensorReadingDto() {
    return new SensorReadingDto(
            SENSOR_ID,
            23.45,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    );
  }


}
