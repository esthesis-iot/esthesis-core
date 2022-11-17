package esthesis.service.command.impl.serialization;

import esthesis.avro.EsthesisCommandRequestMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serializer for EsthesisCommandRequestMessage.
 */
public class EsthesisCommandRequestMessageSerializer implements
    Serializer<EsthesisCommandRequestMessage> {

  private final DatumWriter<EsthesisCommandRequestMessage> datumWriter =
      new SpecificDatumWriter<>(EsthesisCommandRequestMessage.class);

  @Override
  public byte[] serialize(String topic, EsthesisCommandRequestMessage msg) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream,
          null);
      datumWriter.write(msg, encoder);
      encoder.flush();
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
