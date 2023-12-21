package esthesis.common.data;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class MapUtils {

  private MapUtils() {
  }

  /**
   * A utility method to convert a map to a stream of entries while flattening the keys of the map.
   * <p>
   * Example:
   * <pre>
   *   return <my map>.entrySet().stream()
   *         .flatMap(MapUtils::flatten)
   *         .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
   * </pre>
   *
   * @param map The map to flatten.
   * @return Returns a stream of the map with flattened keys.
   */
  public static Stream<Entry<String, Object>> flatten(
      Map.Entry<String, Object> map) {
    if (map.getValue() instanceof Map<?, ?>) {
      Map<String, Object> nested = (Map<String, Object>) map.getValue();

      return nested.entrySet().stream()
          .map(e -> new AbstractMap.SimpleEntry(
              map.getKey() + "." + e.getKey(), e.getValue()))
          .flatMap(MapUtils::flatten);
    }
    return Stream.of(map);
  }
}
