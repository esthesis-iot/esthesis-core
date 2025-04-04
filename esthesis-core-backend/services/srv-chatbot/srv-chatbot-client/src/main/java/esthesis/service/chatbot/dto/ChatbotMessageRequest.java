package esthesis.service.chatbot.dto;

import java.time.Instant;
import lombok.Data;

/**
 * A DTO representing chatbot message reeuqests.
 */
@Data
public class ChatbotMessageRequest {

	private String message;
	private Instant timestamp;
}
