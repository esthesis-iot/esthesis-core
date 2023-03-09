package esthesis.avro.util;

import esthesis.avro.EsthesisCommandReplyMessage;
import esthesis.avro.EsthesisCommandRequestMessage;
import esthesis.avro.EsthesisDataMessage;
import esthesis.avro.PayloadData;
import esthesis.avro.ReplyType;
import esthesis.avro.ValueData;
import esthesis.avro.ValueTypeEnum;
import esthesis.common.AppConstants;
import esthesis.common.data.ValueUtils;
import esthesis.common.data.ValueUtils.ValueType;
import esthesis.common.exception.QMismatchException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@ApplicationScoped
public class AvroUtils {

  /**
   * Sets the value and the value type of the given value data. See {@link #parsePayload(String)}.
   *
   * @param val     The value to introspect.
   * @param builder The builder to set the resulting values to
   */
  private ValueData.Builder setValue(String val, ValueData.Builder builder) {
    ValueType valueType = ValueUtils.detectWithHints(val);
    String extractedVal;
    switch (valueType) {
      case STRING:
        if (val.startsWith("'") && val.endsWith("'")) {
          extractedVal = val.substring(1, val.length() - 1);
        } else {
          extractedVal = val;
        }
        break;
      case  BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE:
        extractedVal = val.substring(0, val.length() - 1);
        break;
      case BIG_INTEGER, BIG_DECIMAL:
        extractedVal = val.substring(0, val.length() - 2);
        break;
      case BOOLEAN, UNKNOWN:
        extractedVal = val;
        break;
      default:
        throw new QMismatchException("Unknown value type '{}'.", valueType);
    }
    return builder.setValue(extractedVal).setValueType(ValueTypeEnum.valueOf(valueType.name()));
  }

  /**
   * Parses a line representing esthesis line protocol into a payload data object for
   * {@link EsthesisDataMessage}. The format of the line protocol is:
   * <pre>
   *   category measurement1=value1[,measurement2=value2...] [timestamp]
   * </pre>
   * <p>
   * The two spaces above indicate the split positions between the category and the measurements,
   * and the measurements and the timestamp. Category and measurement names can not contain spaces.
   * Measurement values can contain spaces if they are included in double quotes.
   * <p>
   * The timestamp component should be expressed as a string, following
   * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a>.
   * <p>
   * All measurement values will be set as Strings in {@link PayloadData}, it is up to the component
   * receiving the resulting  message to convert them to the correct format for its supported data
   * storage implementation.
   * <p>
   * To facilitate downstream data processors, this method will try to detect the type of the value
   * and set it in the `valueType` attribute of the resulting message. You can enforce a specific
   * data type using the following conventions:
   * <ul>
   *   <li>Integer: append a 'i' to the value, e.g. 123i</li>
   *   <li>Float: append a 'f' to the value, e.g. 123.456f</li>
   *   <li>Long: append a 'l' to the value, e.g. 1234567890123456789l</li>
   *   <li>Double: append a 'd' to the value, e.g. 123.456d</li>
   *   <li>Short: append a 's' to the value, e.g. 123s</li>
   *   <li>Byte: append a 'b' to the value, e.g. 123b</li>
   *   <li>Boolean: e.g. true or false</li>
   *   <li>String: enclose the value is single quotes</li>
   * </ul>
   * <p>
   * Any value other than Boolean and String not conforming to the above
   * conventions will be treated as an Integer.
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
      throw new QMismatchException("Requested to parse a comment line '{}', skipping it.",
          StringUtils.abbreviate(line,  AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));
    }

    // Split the line into category, measurements, and optional timestamp.
    @SuppressWarnings("java:S5998") String[] parts = line.split(
        " +(?=((.*?(?<!\\\\)\"){2})*[^\"]*$)");

    if (parts.length < 2) {
      throw new QMismatchException(
          "Invalid line protocol data in line '{}', at least two parts are "
              + "required, the category and one or more measurements.",
          StringUtils.abbreviate(line, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));
    }

    // Start processing each part of the payload.
    log.debug("Processing line '{}'.",
        StringUtils.abbreviate(line, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));
    PayloadData.Builder payloadBuilder = EsthesisDataMessage.newBuilder().getPayloadBuilder();

    // Set the category.
    payloadBuilder.setCategory(parts[0]);

    // Set the measurements.
    String[] measurements = parts[1].split(",");
    payloadBuilder.setValues(Arrays.stream(measurements).map(measurement -> {
      String[] measurementParts = measurement.split("=");
      if (measurementParts.length != 2) {
        throw new QMismatchException("Invalid measurement data in line '{}', expected a key-value "
            + "pair separated by '=', but got '{}'.",
            StringUtils.abbreviate(line, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH), measurement);
      }
      return setValue(measurementParts[1],
          ValueData.newBuilder().setName(measurementParts[0])).build();
    }).toList());

    // Set the timestamp, if available.
    if (parts.length == 3) {
      payloadBuilder.setTimestamp(parts[2]);
    } else {
      payloadBuilder.setTimestamp(Instant.now().toString());
    }

    return payloadBuilder.build();
  }

  /**
   * Parses a Command Reply message expressed in esthesis line protocol into an
   * {@link EsthesisCommandReplyMessage}.
   *
   * @param body       The body of the command reply message.
   * @param hardwareId The hardware ID of the device that sent the reply.
   * @param appName    The application name that creates this reply object.
   * @param topic      The topic on which the reply was received.
   */
  public EsthesisCommandReplyMessage parseCommandReplyLP(String body, String hardwareId,
      String appName, String topic) {
    // Parse the command reply message.
    try {
      String correlationId = body.substring(0, body.indexOf(" "));
      String success = body.substring(body.indexOf(" ") + 1, body.indexOf(" ") + 2);
      String payload = body.substring(body.indexOf(" ") + 3);

      log.debug("Extracted correlation ID '{}', success '{}', and payload '{}'.", correlationId,
          success, StringUtils.abbreviate(payload, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));

      // Convert incoming message to an EsthesisCommandReplyMessage.
      EsthesisCommandReplyMessage msg = EsthesisCommandReplyMessage.newBuilder()
          .setId(UUID.randomUUID().toString()).setCorrelationId(correlationId)
          .setHardwareId(hardwareId).setSeenAt(Instant.now().toString()).setSeenBy(appName)
          .setChannel(topic).setType(ReplyType.valueOf(success)).setPayload(payload).build();

      log.debug("Parsed Command Reply message to EsthesisCommandReplyMessage '{}'",
          StringUtils.abbreviate(msg.toString(), AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));

      return msg;
    } catch (Exception e) {
      throw new QMismatchException("Failed to parse Command Reply message '{}' due to '{}'. "
          + "Check that Command Reply message are formatted as [correlationId] "
          + "[success] [output].", StringUtils.abbreviate(body, AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH), e);
    }
  }

  /**
   * Converts an {@link EsthesisCommandRequestMessage} to the line protocol format.
   *
   * @param msg The Command Request message to convert.
   */
  public String commandRequestToLineProtocol(EsthesisCommandRequestMessage msg) {
    StringBuilder lineProtocol = new StringBuilder();

    // Add the id of this request message.
    lineProtocol.append(msg.getId());
    lineProtocol.append(" ");

    // Add the type of the command.
    lineProtocol.append(msg.getCommandType().toString());

    // Add the execution type of the command.
    lineProtocol.append(msg.getExecutionType().toString());

    if (StringUtils.isNotBlank(msg.getCommand())) {
      lineProtocol.append(" ");
      lineProtocol.append(msg.getCommand());
      if (StringUtils.isNotBlank(msg.getArguments())) {
        lineProtocol.append(" ");
        lineProtocol.append(msg.getArguments());
      }
    }

    log.debug("Converted Avro Command Request message '{}' to line protocol " + "message '{}'.",
        StringUtils.abbreviate(msg.toString(), AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH),
        StringUtils.abbreviate(lineProtocol.toString(), AppConstants.MESSAGE_LOG_ABBREVIATION_LENGTH));

    return lineProtocol.toString();
  }

}
