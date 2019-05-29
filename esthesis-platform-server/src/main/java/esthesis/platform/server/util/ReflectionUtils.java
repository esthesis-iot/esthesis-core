package esthesis.platform.server.util;


import com.eurodyn.qlack.common.util.KeyValue;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReflectionUtils {
  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ReflectionUtils.class.getName());

  public static List<KeyValue<String, String>> getStaticFieldsValue(Class clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
      .filter(f -> Modifier.isStatic(f.getModifiers()))
      .map(f -> {
        try {
          return new com.eurodyn.qlack.common.util.KeyValue<>(f.getName(), (String)f.get(null));
        } catch (IllegalAccessException e) {
          LOGGER.log(Level.SEVERE,  e.getMessage(), e);
          return null;
        }
      })
      .sorted(Comparator.comparing(o -> o.getValue().toLowerCase()))
      .collect(Collectors.toList());
  }
}
