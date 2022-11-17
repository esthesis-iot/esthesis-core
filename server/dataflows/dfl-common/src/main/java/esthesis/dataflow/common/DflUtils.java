package esthesis.dataflow.common;

import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class DflUtils {

  // The size limit when displaying message content related information in the
  // logs.
  public static final int MESSAGE_LOG_ABBREVIATION_LENGTH = 4096;

  /**
   * Helper method to abbreviate output messages to a specific length.
   *
   * @param msg The message to abbreviate.
   */
  public static String abbr(String msg) {
    return StringUtils.abbreviate(msg, MESSAGE_LOG_ABBREVIATION_LENGTH);
  }

  // An enum with different types of values, used by dataflows that need to
  // identify the type of the value they carry.
  public enum VALUE_TYPE {
    STRING, BOOLEAN, BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE
  }

}
