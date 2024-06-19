package esthesis.avro.util.camel;

import esthesis.avro.EsthesisCommandRequestMessage;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.support.service.ServiceSupport;

public class EsthesisCommandRequestDataFormat extends ServiceSupport implements DataFormat {

 @Override
 public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
	 EsthesisCommandRequestMessage.getEncoder().encode((EsthesisCommandRequestMessage) graph, stream);
 }

 @Override
 public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
  return EsthesisCommandRequestMessage.getDecoder().decode(stream);
 }

 public static EsthesisCommandRequestDataFormat create() {
	return new EsthesisCommandRequestDataFormat();
 }
}
