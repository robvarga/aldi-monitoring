package com.aldisued.iot.monitoring.service;

public abstract class ServiceFailedException extends RuntimeException {

  public ServiceFailedException(String message) {
    super(message);
  }

  public ServiceFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceFailedException(Throwable cause) {
    super(cause);
  }

  protected ServiceFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
