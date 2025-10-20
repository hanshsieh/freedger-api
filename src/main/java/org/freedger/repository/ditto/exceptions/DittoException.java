package org.freedger.repository.ditto.exceptions;

public class DittoException extends Exception {
  public DittoException(String message, Throwable cause) {
    super(message, cause);
  }

  public DittoException(String message) {
    super(message);
  }
}
