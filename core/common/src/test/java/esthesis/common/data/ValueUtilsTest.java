package esthesis.common.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import esthesis.common.data.ValueUtils.ValueType;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class ValueUtilsTest {

  @Test
  void testDetect() {
    assertEquals(ValueType.STRING, ValueUtils.detect("helloworld"));
    assertEquals(ValueType.BOOLEAN, ValueUtils.detect("true"));
    assertEquals(ValueType.BOOLEAN, ValueUtils.detect("TRUE"));
    assertEquals(ValueType.BOOLEAN, ValueUtils.detect("false"));
    assertEquals(ValueType.BOOLEAN, ValueUtils.detect("FALSE"));

    assertEquals(ValueType.BYTE, ValueUtils.detect(String.valueOf(Byte.MIN_VALUE)));
    assertEquals(ValueType.BYTE, ValueUtils.detect(String.valueOf(Byte.MAX_VALUE)));
    assertEquals(ValueType.SHORT, ValueUtils.detect(String.valueOf(Short.MIN_VALUE)));
    assertEquals(ValueType.SHORT, ValueUtils.detect(String.valueOf(Short.MAX_VALUE)));
    assertEquals(ValueType.INTEGER, ValueUtils.detect(String.valueOf(Integer.MIN_VALUE)));
    assertEquals(ValueType.INTEGER, ValueUtils.detect(String.valueOf(Integer.MAX_VALUE)));
    assertEquals(ValueType.LONG, ValueUtils.detect(String.valueOf(Long.MIN_VALUE)));
    assertEquals(ValueType.LONG, ValueUtils.detect(String.valueOf(Long.MAX_VALUE)));

    assertEquals(ValueType.FLOAT, ValueUtils.detect(String.valueOf(Float.MIN_VALUE)));
    assertEquals(ValueType.FLOAT, ValueUtils.detect(String.valueOf(Float.MAX_VALUE)));
    assertEquals(ValueType.DOUBLE, ValueUtils.detect(String.valueOf(Double.MIN_VALUE)));
    assertEquals(ValueType.DOUBLE, ValueUtils.detect(String.valueOf(Double.MAX_VALUE)));

    assertEquals(ValueType.BIG_INTEGER,
        ValueUtils.detect(String.valueOf(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE))));
    assertEquals(ValueType.BIG_DECIMAL, ValueUtils.detect(
        String.valueOf(BigDecimal.valueOf(Double.MAX_VALUE).add(new BigDecimal(Float.MIN_VALUE)))));
  }

}
