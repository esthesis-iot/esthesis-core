package esthesis.dataflow.common;

import esthesis.core.common.exception.QDoesNotExistException;
import esthesis.core.common.exception.QExceptionWrapper;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class RestClientExceptionMapper implements ResponseExceptionMapper<Exception> {

	@Override
	public Exception toThrowable(Response response) {
		int code = response.getStatus();

		if (code == 404) {
			throw new QDoesNotExistException(response.getStatusInfo().getReasonPhrase());
		} else {
			throw new QExceptionWrapper(response.getStatusInfo().getReasonPhrase());
		}
	}
}
