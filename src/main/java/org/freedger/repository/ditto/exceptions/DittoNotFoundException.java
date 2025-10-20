package org.freedger.repository.ditto.exceptions;

public class DittoNotFoundException extends DittoException {
  public DittoNotFoundException(String message) {
    super(message);
  }

  public DittoNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
