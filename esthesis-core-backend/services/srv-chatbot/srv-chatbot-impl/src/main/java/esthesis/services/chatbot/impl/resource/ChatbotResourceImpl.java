package esthesis.services.chatbot.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.chatbot.dto.ChatbotMessageRequest;
import esthesis.service.chatbot.resource.ChatbotResource;
import esthesis.services.chatbot.impl.service.ChatbotService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of the ChatbotResource interface.
 */
@RequiredArgsConstructor
public class ChatbotResourceImpl implements ChatbotResource {

	private final ChatbotService chatbotService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public Response receiveMessage(ChatbotMessageRequest request) {
		System.out.println(request);
		return null;
	}
}
