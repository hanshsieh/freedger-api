package org.freedger.domain.exception;

public class FreedgerException extends Exception {
  public FreedgerException(String message) {
    super(message);
  }

  public FreedgerException(String message, Throwable cause) {
    super(message, cause);
  }
}
