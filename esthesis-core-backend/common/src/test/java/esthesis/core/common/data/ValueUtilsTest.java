package esthesis.core.common.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import esthesis.common.data.DataUtils;
import esthesis.common.data.DataUtils.ValueType;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class DataUtilsTest {

  @Test
  void testDetect() {
    assertEquals(ValueType.STRING, DataUtils.detectValueType("helloworld"));
    assertEquals(ValueType.BOOLEAN, DataUtils.detectValueType("true"));
    assertEquals(ValueType.BOOLEAN, DataUtils.detectValueType("TRUE"));
    assertEquals(ValueType.BOOLEAN, DataUtils.detectValueType("false"));
    assertEquals(ValueType.BOOLEAN, DataUtils.detectValueType("FALSE"));

    assertEquals(ValueType.BYTE, DataUtils.detectValueType(String.valueOf(Byte.MIN_VALUE)));
    assertEquals(ValueType.BYTE, DataUtils.detectValueType(String.valueOf(Byte.MAX_VALUE)));
    assertEquals(ValueType.SHORT, DataUtils.detectValueType(String.valueOf(Short.MIN_VALUE)));
    assertEquals(ValueType.SHORT, DataUtils.detectValueType(String.valueOf(Short.MAX_VALUE)));
    assertEquals(ValueType.INTEGER, DataUtils.detectValueType(String.valueOf(Integer.MIN_VALUE)));
    assertEquals(ValueType.INTEGER, DataUtils.detectValueType(String.valueOf(Integer.MAX_VALUE)));
    assertEquals(ValueType.LONG, DataUtils.detectValueType(String.valueOf(Long.MIN_VALUE)));
    assertEquals(ValueType.LONG, DataUtils.detectValueType(String.valueOf(Long.MAX_VALUE)));

    assertEquals(ValueType.FLOAT, DataUtils.detectValueType(String.valueOf(Float.MIN_VALUE)));
    assertEquals(ValueType.FLOAT, DataUtils.detectValueType(String.valueOf(Float.MAX_VALUE)));
    assertEquals(ValueType.DOUBLE, DataUtils.detectValueType(String.valueOf(Double.MIN_VALUE)));
    assertEquals(ValueType.DOUBLE, DataUtils.detectValueType(String.valueOf(Double.MAX_VALUE)));

    assertEquals(ValueType.BIG_INTEGER,
        DataUtils.detectValueType(String.valueOf(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE))));
    assertEquals(ValueType.BIG_DECIMAL, DataUtils.detectValueType(
        String.valueOf(BigDecimal.valueOf(Double.MAX_VALUE).add(new BigDecimal(Float.MIN_VALUE)))));
  }
}
