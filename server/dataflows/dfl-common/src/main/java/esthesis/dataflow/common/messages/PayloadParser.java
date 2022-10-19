package esthesis.dataflow.common.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * A utility class allowing you to process telemetry and metadata payload types
 * of an {@link EsthesisMessage} obtaining the measurement, the field, and the
 * value contained within the payload.
 */
@Data
public class PayloadParser {

  private String measurement;
  private String field;
  private Object value;

  private static Pattern pattern =
      Pattern.compile("\\$(.*)\\.(.*)=(.*)", Pattern.CASE_INSENSITIVE);

  private static void extractValue(PayloadParser payloadParser, String value) {
    if (NumberUtils.isParsable(value)) {
      payloadParser.setValue(NumberUtils.createNumber(value));
    } else {
      if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
        payloadParser.setValue(Boolean.parseBoolean(value));
      } else {
        payloadParser.setValue(value);
      }
    }
  }

  public static PayloadParser parse(String payloaad) {
    PayloadParser payloadParser = null;
    Matcher matcher = pattern.matcher(payloaad);
    if (matcher.find()) {
      payloadParser = new PayloadParser();
      payloadParser.setMeasurement(matcher.group(1));
      payloadParser.setField(matcher.group(2));
      extractValue(payloadParser, matcher.group(3));
    }

    return payloadParser;
  }
}
