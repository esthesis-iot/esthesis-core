package esthesis.platform.server.util;


import com.eurodyn.qlack.common.util.KeyValue;
import lombok.extern.java.Log;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class ReflectionUtils {

  private ReflectionUtils() {
  }

  public static List<KeyValue<String, String>> getStaticFieldsValue(Class clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
      .filter(f -> Modifier.isStatic(f.getModifiers()))
      .map(f -> {
        try {
          return new com.eurodyn.qlack.common.util.KeyValue<>(f.getName(), (String)f.get(null));
        } catch (IllegalAccessException e) {
          log.log(Level.SEVERE,  e.getMessage(), e);
          return null;
        }
      })
      .sorted(Comparator.comparing(o -> o.getValue().toLowerCase()))
      .collect(Collectors.toList());
  }
}
