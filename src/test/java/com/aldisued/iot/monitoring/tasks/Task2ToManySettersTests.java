package com.aldisued.iot.monitoring.tasks;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Sql(scripts = "/sql/task-2-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class Task2ToManySettersTests extends IntegrationTestBase {

  private static final UUID TEMPERATURE_SENSOR_ID = UUID.fromString(
          "e3242ea2-0514-46d3-aad8-b2012980c41c");


  @PersistenceContext
  EntityManager entityManager;

  @Autowired
  private SensorRepository sensorRepository;
  @Autowired
  private SensorReadingRepository sensorReadingRepository;
  @Autowired
  private AlertRepository alertRepository;

  @BeforeEach
  public void setup() {
  }

  @AfterEach
  public void tearDown() {
    sensorRepository.deleteAll();
  }

  @Test
  @Transactional
  public void verifySetSensorReadingsWork() {

    Sensor sensor = inTransaction(temperatureSensor -> {
      List<SensorReading> coll = new ArrayList<>(temperatureSensor.getSensorReadings());
      coll.removeIf(reading -> reading.getId() == 2L);
      coll.add(new SensorReading(10.0, LocalDateTime.now(), temperatureSensor));
      temperatureSensor.setSensorReadings(coll);

      sensorRepository.saveAndFlush(temperatureSensor);
    });

    List<SensorReading> sensorReadings = sensor.getSensorReadings();
    Assertions.assertEquals(3, sensorReadings.size());
    Set<Long> ids = sensorReadings.stream().map(SensorReading::getId).collect(Collectors.toSet());
    Assertions.assertTrue(ids.contains(1L));
    Assertions.assertTrue(ids.contains(3L));
    ids.remove(1L);
    ids.remove(3L);
    Assertions.assertFalse(ids.contains(1L));
    Assertions.assertFalse(ids.contains(3L));
    Assertions.assertEquals(1, ids.size());
  }


  @Test
  @Transactional
  public void verifySetAlertsWork() {

    Sensor sensor = inTransaction(temperatureSensor -> {
      List<Alert> coll = new ArrayList<>(temperatureSensor.getAlerts());
      coll.removeIf(reading -> reading.getId() == 1L);
      coll.add(new Alert("alert message", LocalDateTime.now(), temperatureSensor));
      temperatureSensor.setAlerts(coll);

      sensorRepository.saveAndFlush(temperatureSensor);
    });

    List<Alert> alerts = sensor.getAlerts();
    Assertions.assertEquals(1, alerts.size());
    Set<Long> ids = alerts.stream().map(Alert::getId).collect(Collectors.toSet());
    Assertions.assertFalse(ids.contains(1L));
  }


  Sensor inTransaction(Consumer<Sensor> runnable) {
    if (runnable != null) {
      Sensor sensor = sensorRepository.findById(TEMPERATURE_SENSOR_ID)
              .orElseThrow();
      runnable.accept(sensor);
    }
    entityManager.flush();
    entityManager.clear();
    TestTransaction.flagForCommit();
    TestTransaction.end();      // commits

    TestTransaction.start();    // fresh transaction + persistence context
    Sensor sensor = sensorRepository.findById(TEMPERATURE_SENSOR_ID)
            .orElseThrow();
    sensor.getSensorReadings().forEach(SensorReading::getValue);
    sensor.getAlerts().forEach(Alert::getTimestamp);
    return sensor;
  }

}
