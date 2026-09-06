package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, Long> {

  static final String FQN = "com.aldisued.iot.monitoring.dto.AlertDto";

  static boolean classNamesMatch() {
    return FQN.equals(AlertDto.class.getName());
  }

  /**
   * This method would be much more usable if AlertDto also contained the alert's ID.
   *
   * @param sensorId id of the sensor to get the latest alert for.
   * @return the latest alert for the given sensor, or an empty Optional if no alert exists.
   */
  @Query("""
          select new
          """ + " " + FQN + """
          (a.sensor.id, a.message, a.timestamp)
          from Alert a
          where a.sensor.id = :sensorId
          order by a.timestamp desc
          limit 1
          """)
  Optional<AlertDto> findLatestAlertBySensorId(UUID sensorId);
}
