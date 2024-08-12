package esthesis.dataflows.oriongateway.client;

import esthesis.dataflows.oriongateway.service.OrionAuthService;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class OrionClientHeaderFilter implements ClientRequestFilter {

	private static final String LINK_HEADER_NAME = "Link";
	private static final String TENANT_HEADER_NAME = "NGSILD-Tenant";
	private final String linkHeaderValue;
	private final OrionAuthService authService;
	private final String tenantHeaderValue;

	public OrionClientHeaderFilter(List<String> contextsUrl, List<String> rel, OrionAuthService authService, String tenantHeaderValue) {
		this.authService = authService;
		this.tenantHeaderValue = tenantHeaderValue;
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
	public void filter(ClientRequestContext clientRequestContext){
		clientRequestContext.getHeaders().add(LINK_HEADER_NAME, linkHeaderValue);
		if(StringUtils.isNotBlank(this.tenantHeaderValue)) {
			clientRequestContext.getHeaders().add(TENANT_HEADER_NAME, tenantHeaderValue);
		}
		authService.authenticate(clientRequestContext);
	}
}
