package esthesis.avro.util.camel;

import esthesis.avro.EsthesisDataMessage;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.support.service.ServiceSupport;

public class EsthesisDataMessageDataFormat extends ServiceSupport implements DataFormat {

	@Override
	public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
		EsthesisDataMessage.getEncoder().encode((EsthesisDataMessage) graph, stream);
	}

	@Override
	public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
		return EsthesisDataMessage.getDecoder().decode(stream);
	}

	public static EsthesisDataMessageDataFormat create() {
		return new EsthesisDataMessageDataFormat();
	}
}
