package com.aldisued.iot.monitoring.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MeasurementCalculatorService {

  public List<Double> filterByAverageDeviation(List<Double> values, Double deviation) {
    if (deviation == null || deviation < 0.0 || deviation > 1.0) {
      throw new IllegalArgumentException("Invalid deviation: " + deviation);
    }
    if (values.isEmpty()) {
      return Collections.emptyList();
    }
    // orElse will not be hit, we already returned if it were empty
    double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    double permittedDiff = Math.abs(avg * deviation);
    double minPermitted = avg - permittedDiff;
    double maxPermitted = avg + permittedDiff;
    return values.stream().mapToDouble(Double::doubleValue).filter(v -> v >= minPermitted && v <= maxPermitted)
            .boxed().toList();
  }

  public List<Double> getMovingAverage(List<Double> data, int windowSize) {
    int size = data.size();
    if (size < windowSize || windowSize <= 0) {
      throw new IllegalArgumentException("Invalid window size: " + windowSize + ", data size: " + size);
    }

    // could be implemented in Java 25 with a sliding window gatherer

    List<Double> res = new ArrayList<>(size - windowSize + 1);
    double rollingSum = 0.0;
    for (int i = 0; i < windowSize; ++i) {
      rollingSum += data.get(i);
    }
    res.add(rollingSum / windowSize);
    for (int i = windowSize; i < size; ++i) {
      rollingSum -= data.get(i - windowSize);
      rollingSum += data.get(i);
      res.add(rollingSum / windowSize);
    }
    return res;
  }

}
