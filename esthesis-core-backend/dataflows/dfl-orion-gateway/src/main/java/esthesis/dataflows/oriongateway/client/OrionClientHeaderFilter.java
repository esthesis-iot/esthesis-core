package esthesis.dataflows.oriongateway.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class OrionClientHeaderFilter implements ClientRequestFilter {

	private static final String LINK_HEADER_NAME = "Link";
	private final String linkHeaderValue;

	public OrionClientHeaderFilter(List<String> contextsUrl, List<String> rel) {
		// Create link header value from contexts and relationship values configured
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < contextsUrl.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(String.format("<%s>; rel=\"%s\"", contextsUrl.get(i), rel.get(i)));
		}
		this.linkHeaderValue = builder.toString();
	}

	@Override
	public void filter(ClientRequestContext clientRequestContext) throws IOException {
		clientRequestContext.getHeaders().add(LINK_HEADER_NAME, linkHeaderValue);
	}
}
