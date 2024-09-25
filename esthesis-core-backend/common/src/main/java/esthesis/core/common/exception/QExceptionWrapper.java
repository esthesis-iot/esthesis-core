package esthesis.core.common.exception;

/**
 * A generic exception wrapper.
 *
 * @author European Dynamics SA
 */
public class QExceptionWrapper extends QException {

  public QExceptionWrapper() {
    super();
  }

  public QExceptionWrapper(String message) {
    super(message);
  }

  public QExceptionWrapper(String message, Throwable cause) {
    super(message, cause);
  }

  public QExceptionWrapper(String message, Object... args) {
    super(message, args);
  }
}
