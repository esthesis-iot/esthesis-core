package esthesis.dataflow.common;

import esthesis.common.exception.QMismatchException;
import esthesis.dataflow.common.parser.EsthesisMessage;
import esthesis.dataflow.common.parser.PayloadData;
import esthesis.dataflow.common.parser.ValueData;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class DflUtils {

  // The size limit when displaying message content related information in the
  // logs.
  public static final int MESSAGE_LOG_ABBREVIATION_LENGTH = 100;

  /**
   * Parses a line representing esthesis line protocol into a payload data
   * object for {@link EsthesisMessage}. The format of the line protocol is:
   * <pre>
   *   category measurement1=value1[,measurement2=value2...] [timestamp]
   * </pre>
   * <p>
   * The two spaces above indicate the split positions between the category and
   * the measurements, and the measurements and the timestamp. Category and
   * measurement names can not contain spaces. Measurement values can contain
   * spaces if they are included in double quotes.
   * <p>
   * The timestamp component should be expressed as a string, following
   * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a>.
   * <p>
   * All measurement values will be set as Strings in {@link PayloadData}, it is
   * up to the component receiving the resulting  message to convert them to the
   * correct format for its supported data storage implementation.
   * <p>
   * Examples:
   * <pre>
   *   cpu load=1
   *   cpu load=1 2022-01-01T01:02:03Z
   *   cpu load=1,temperature=20
   *   cpu load=1,temperature=20 2022-01-01T01:02:03Z
   *   net ip1="primary 192.168.1.1"
   *   net ip1="primary 192.168.1.1" 2022-01-01T01:02:03Z
   *   net ip1="primary 192.168.1.1",ip2="secondary 10.250.1.1"
   *   net ip1="primary 192.168.1.1",ip2="secondary 10.250.1.1" 2022-01-01T01:02:03Z
   * </pre>
   *
   * @param line The line to parse.
   * @return The parsed payload data.
   */
  public PayloadData parsePayload(final String line) {
    // Skip comment lines.
    if (line.startsWith("#")) {
      throw new QMismatchException(
          "Requested to parse a comment line '{}', skipping it.",
          StringUtils.abbreviate(line,
              DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH));
    }

    // Split the line into category, measurements, and optional timestamp.
    String[] parts = line.split(" +(?=((.*?(?<!\\\\)\"){2})*[^\"]*$)");

    if (parts.length < 2) {
      throw new QMismatchException(
          "Invalid line protocol data in line '{}', at least two parts are "
              + "required, the category and one or more measurements.",
          StringUtils.abbreviate(line,
              DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH));
    }

    // Start processing each part of the payload.
    log.debug("Processing line '{}'.",
        StringUtils.abbreviate(line, DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH));
    esthesis.dataflow.common.parser.PayloadData.Builder payloadBuilder =
        EsthesisMessage.newBuilder().getPayloadBuilder();

    // Set the category.
    payloadBuilder.setCategory(parts[0]);

    // Set the measurements.
    String[] measurements = parts[1].split(",");
    payloadBuilder.setValues(Arrays.stream(measurements)
        .map(measurement -> {
          String[] measurementParts = measurement.split("=");
          if (measurementParts.length != 2) {
            throw new QMismatchException(
                "Invalid measurement data in line '{}', expected a key-value "
                    + "pair separated by '=', but got '{}'.",
                StringUtils.abbreviate(line,
                    DflUtils.MESSAGE_LOG_ABBREVIATION_LENGTH),
                measurement);
          }
          return ValueData.newBuilder()
              .setName(measurementParts[0])
              .setValue(StringUtils.strip(measurementParts[1], "\""))
              .build();
        })
        .collect(Collectors.toList()));

    // Set the timestamp, if available.
    if (parts.length == 3) {
      payloadBuilder.setTimestamp(parts[2]);
    } else {
      payloadBuilder.setTimestamp(Instant.now().toString());
    }

    return payloadBuilder.build();
  }
}
