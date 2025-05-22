package esthesis.services.chatbot.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.chatbot.dto.ChatbotMessageDTO;
import esthesis.services.chatbot.impl.service.ChatbotAgentService;
import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import jakarta.annotation.security.RolesAllowed;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@BearerTokenAuthentication
@WebSocket(path = "/api/v1")
public class ChatboxWebSocketV1 {

	private final ChatbotAgentService chatbotAgentService;
	private final SecurityIdentity securityIdentity;

	@OnOpen
	public ChatbotMessageDTO onOpen() {
		return new ChatbotMessageDTO()
			.setMessage("Welcome to esthesis bot! How can I help?")
			.setCorrelationId(UUID.randomUUID().toString())
			.setTimestamp(Instant.now().toEpochMilli());
	}

	@OnTextMessage
	@RolesAllowed(AppConstants.ROLE_USER)
//	public Multi<ChatbotMessageDTO> onTextMessage(ChatbotMessageDTO message) {
	public ChatbotMessageDTO onTextMessage(ChatbotMessageDTO message) {
		String correlationId = UUID.randomUUID().toString();
//		return chatbotAgentService.chat(securityIdentity.getPrincipal().getName(),
//			message.getMessage()).map(
//			reply -> new ChatbotMessageDTO()
//				.setMessage(reply)
//				.setCorrelationId(correlationId)
//				.setTimestamp(Instant.now().toEpochMilli())
//				.setUserInput(false)
//		);
		String reply = chatbotAgentService.chat(securityIdentity.getPrincipal().getName(),
			message.getMessage());
		return new ChatbotMessageDTO()
				.setMessage(reply)
				.setCorrelationId(correlationId)
				.setTimestamp(Instant.now().toEpochMilli())
				.setUserInput(false);
	}

	@OnPingMessage
	@RolesAllowed(AppConstants.ROLE_USER)
	Uni<Void> ping(Buffer data) {
		return Uni.createFrom().voidItem();
	}
}
