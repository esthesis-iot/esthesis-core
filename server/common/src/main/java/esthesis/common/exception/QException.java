package esthesis.common.exception;

import java.io.Serializable;

/**
 * Parent exception type.
 *
 * @author European Dynamics SA
 */
public abstract class QException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = 4808786528779863568L;

  protected QException() {
  }

  protected QException(String message) {
    super(message);
  }

  protected QException(String message, Object... args) {
    super(org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage());
  }

  protected QException(String message, Throwable cause) {
    super(message, cause);
  }

  protected QException(String message, Throwable cause, Object... args) {
    super(org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage(), cause);
  }

  protected QException(Throwable cause) {
    super(cause);
  }

  protected QException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
