package esthesis.core.common.serder.camel;

import esthesis.common.avro.EsthesisCommandReplyMessage;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.support.service.ServiceSupport;

public class EsthesisCommandReplyDataFormat extends ServiceSupport implements DataFormat {

 @Override
 public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
	 EsthesisCommandReplyMessage.getEncoder().encode((EsthesisCommandReplyMessage) graph, stream);
 }

 @Override
 public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
  return EsthesisCommandReplyMessage.getDecoder().decode(stream);
 }

 public static EsthesisCommandReplyDataFormat create() {
	return new EsthesisCommandReplyDataFormat();
 }
}
