package esthesis.common.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import esthesis.common.data.ValueUtils.ValueType;
import org.junit.jupiter.api.Test;

class ValueUtilsTest {

  @Test
  void testDetect() {
    ValueUtils valueUtils = new ValueUtils();

    assertEquals(ValueType.STRING, valueUtils.detect("helloworld"));
    assertEquals(ValueType.BOOLEAN, valueUtils.detect("true"));
    assertEquals(ValueType.BOOLEAN, valueUtils.detect("TRUE"));
    assertEquals(ValueType.BOOLEAN, valueUtils.detect("false"));
    assertEquals(ValueType.BOOLEAN, valueUtils.detect("FALSE"));

    assertEquals(ValueType.BYTE, valueUtils.detect(String.valueOf(Byte.MIN_VALUE)));
    assertEquals(ValueType.BYTE, valueUtils.detect(String.valueOf(Byte.MAX_VALUE)));
    assertEquals(ValueType.SHORT, valueUtils.detect(String.valueOf(Short.MIN_VALUE)));
    assertEquals(ValueType.SHORT, valueUtils.detect(String.valueOf(Short.MAX_VALUE)));
    assertEquals(ValueType.INTEGER, valueUtils.detect(String.valueOf(Integer.MIN_VALUE)));
    assertEquals(ValueType.INTEGER, valueUtils.detect(String.valueOf(Integer.MAX_VALUE)));
    assertEquals(ValueType.LONG, valueUtils.detect(String.valueOf(Long.MIN_VALUE)));
    assertEquals(ValueType.LONG, valueUtils.detect(String.valueOf(Long.MAX_VALUE)));
    assertEquals(ValueType.BIGDECIMAL, valueUtils.detect("1.0"));
    assertEquals(ValueType.BIGDECIMAL, valueUtils.detect("-1.0"));
  }

}
