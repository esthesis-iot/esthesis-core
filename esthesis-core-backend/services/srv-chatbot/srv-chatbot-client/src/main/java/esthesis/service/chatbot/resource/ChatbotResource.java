package esthesis.service.chatbot.resource;

import esthesis.service.chatbot.dto.ChatbotMessageRequest;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the Chatbot service.
 */
@Path("/api")
@AccessToken
@RegisterRestClient(configKey = "ChatbotResource")
public interface ChatbotResource {

	@POST
	@Path("/v1")
	Response receiveMessage(@Valid ChatbotMessageRequest request);

}
