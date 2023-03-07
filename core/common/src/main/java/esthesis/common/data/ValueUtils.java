package esthesis.common.data;

import java.math.BigDecimal;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class ValueUtils {

  // A list of value types that cab detected.
  public enum ValueType {
    STRING, BOOLEAN, BYTE, SHORT, INTEGER, LONG, BIGDECIMAL, UNKNOWN
  }

  /**
   * A best-effort method to detect the type of a value.
   *
   * @param val The value to detect the type of.
   */
  public static ValueType detect(String val) {
    if (NumberUtils.isCreatable(val)) {
      try {
        if (val.equals(String.valueOf(Byte.parseByte(val)))) {
          return ValueType.BYTE;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(Short.parseShort(val)))) {
          return ValueType.SHORT;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(Integer.parseInt(val)))) {
          return ValueType.INTEGER;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(Long.parseLong(val)))) {
          return ValueType.LONG;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(new BigDecimal(val)))) {
          return ValueType.BIGDECIMAL;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }

      return ValueType.UNKNOWN;
    } else {
      val = val.toLowerCase();

      if (BooleanUtils.toBooleanObject(val) != null) {
        return ValueType.BOOLEAN;
      } else {
        return ValueType.STRING;
      }
    }
  }
}
