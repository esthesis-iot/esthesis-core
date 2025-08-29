package esthesis.services.chatbot.impl.resource;

import esthesis.service.chatbot.dto.ChatbotMessageDTO;
import esthesis.services.chatbot.impl.service.ChatbotAgentService;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatboxWebSocketV1Test {

	@Mock
	ChatbotAgentService chatbotAgentService;

	@Mock
	SecurityIdentity securityIdentity;

	@Mock
	Principal principal;

	@InjectMocks
	ChatboxWebSocketV1 socket; // uses @RequiredArgsConstructor

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(securityIdentity.getPrincipal()).thenReturn(principal);
		when(principal.getName()).thenReturn("esthesis@test.com");
	}

	@Test
	void onOpen() {
		ChatbotMessageDTO res = socket.onOpen();

		assertNotNull(res);
		assertEquals("Welcome to esthesis bot! How can I help?", res.getMessage());
		assertNotNull(res.getCorrelationId());
		assertFalse(res.getCorrelationId().isBlank());
		assertTrue(res.getTimestamp() > 0);
	}

	@Test
	void onTextMessage() {
		// arrange
		ChatbotMessageDTO incoming = new ChatbotMessageDTO()
			.setMessage("What devices are registered?")
			.setUserInput(true);
		when(chatbotAgentService.chat("esthesis@test.com", "What devices are registered?"))
			.thenReturn("5 devices are registered.");

		// act
		ChatbotMessageDTO res = socket.onTextMessage(incoming);

		// assert agent call
		verify(chatbotAgentService, times(1))
			.chat("esthesis@test.com", "What devices are registered?");

		// assert response envelope
		assertNotNull(res);
		assertEquals("5 devices are registered.", res.getMessage());
		assertNotNull(res.getCorrelationId());
		assertFalse(res.getCorrelationId().isBlank());
		assertTrue(res.getTimestamp() > 0);
	}

	@Test
	void ping() {
		Uni<Void> uni = socket.ping(Buffer.buffer("ping"));
		UniAssertSubscriber<Void> sub = UniAssertSubscriber.create();
		uni.subscribe().withSubscriber(sub);
		sub.assertCompleted();
	}
}
