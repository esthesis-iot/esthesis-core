package esthesis.dataflow.common;

import esthesis.avro.EsthesisCommandReplyMessage;
import esthesis.avro.EsthesisDataMessage;
import org.apache.camel.dataformat.avro.AvroDataFormat;

public class EsthesisAvroFormats {

	private EsthesisAvroFormats() {
	}

	public static AvroDataFormat esthesisDataMessageFormat() {
		return new AvroDataFormat(EsthesisDataMessage.SCHEMA$);
	}

	public static AvroDataFormat esthesisCommandRequestMessageFormat() {
		return new AvroDataFormat(EsthesisCommandReplyMessage.SCHEMA$);
	}

	public static AvroDataFormat esthesisCommandReplyMessageFormat() {
		return new AvroDataFormat(EsthesisCommandReplyMessage.SCHEMA$);
	}
}
