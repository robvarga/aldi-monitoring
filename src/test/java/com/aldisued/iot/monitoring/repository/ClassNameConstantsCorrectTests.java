package com.aldisued.iot.monitoring.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassNameConstantsCorrectTests {

  @Test
  public void enforceClassNameConstantsCorrect() {
    Assertions.assertThat(AlertRepository.classNamesMatch()).isTrue();
  }
}
