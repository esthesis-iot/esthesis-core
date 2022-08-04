package esthesis.common.exception;

import java.io.Serializable;

/**
 * The QException class can be extended by the custom exceptions of the Qlack
 * modules in order to throw error messages on the runtime and rollback the
 * transactions.
 *
 * @author European Dynamics SA
 */
public abstract class QException extends RuntimeException implements
    Serializable {

  private static final long serialVersionUID = 4808786528779863568L;

  protected QException() {
  }

  protected QException(String message) {
    super(message);
  }

  protected QException(String message, Object... args) {
    super(
        org.slf4j.helpers.MessageFormatter.format(message, args).getMessage());
  }

  protected QException(String message, Throwable cause) {
    super(message, cause);
  }

  protected QException(Throwable cause) {
    super(cause);
  }

  protected QException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
