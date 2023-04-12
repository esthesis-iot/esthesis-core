package esthesis.common.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utility functions to determine the type of a value (represented as a String).
 */
public class ValueUtils {

  // A list of value types that cab detected.
  public enum ValueType {
    STRING,
    BOOLEAN,
    BYTE, SHORT, INTEGER, LONG, BIG_INTEGER,
    FLOAT, DOUBLE, BIG_DECIMAL,
    UNKNOWN
  }

  /**
   * A best-effort method to detect the type of a value.
   *
   * @param val The value to detect its type.
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
        if (val.equals(String.valueOf(Float.parseFloat(val)))) {
          return ValueType.FLOAT;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(Double.parseDouble(val)))) {
          return ValueType.DOUBLE;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(new BigInteger(val)))) {
          return ValueType.BIG_INTEGER;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }
      try {
        if (val.equals(String.valueOf(new BigDecimal(val)))) {
          return ValueType.BIG_DECIMAL;
        }
      } catch (NumberFormatException e) {
        // Ignore
      }

      return ValueType.UNKNOWN;
    } else {
      if (BooleanUtils.toBooleanObject(val) != null) {
        return ValueType.BOOLEAN;
      } else {
        return ValueType.STRING;
      }
    }
  }

  /**
   * A best-effort method to detect the type of a value using hints within the variable's value.
   * The following hints are supported:
   * <pre>
   *   - If the value starts and ends with a single quote, it is a String.
   *   - If the value is "true" or "false" (case is ignored), it is a Boolean.
   *   - If the value ends in "b", it is a Byte.
   *   - If the value ends in "s", it is a Short.
   *   - If the value ends in "i", it is an Integer.
   *   - If the value ends in "l", it is a Long.
   *   - If the value ends in "bi", it is a BigInteger.
   *   - If the value ends in "f", it is a Float.
   *   - If the value ends in "d", it is a Double.
   *   - If the value ends in "bd", it is a BigDecimal.
   * </pre>
   * @param val The value to detect its type.
   */
  public static ValueType detectWithHints(String val) {
    if (val.startsWith("'") && val.endsWith("'")) {
      return ValueType.STRING;
    } else if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
      return ValueType.BOOLEAN;
    } else if (val.endsWith("b")) {
      return ValueType.BYTE;
    } else if (val.endsWith("s")) {
      return ValueType.SHORT;
    } else if (val.endsWith("i")) {
      return ValueType.INTEGER;
    } else if (val.endsWith("l")) {
      return ValueType.LONG;
    } else if (val.endsWith("bi")) {
      return ValueType.BIG_INTEGER;
    } else if (val.endsWith("f")) {
      return ValueType.FLOAT;
    } else if (val.endsWith("d")) {
      return ValueType.DOUBLE;
    } else if (val.endsWith("bd")) {
      return ValueType.BIG_DECIMAL;
    } else {
      return detect(val);
    }
  }
}
