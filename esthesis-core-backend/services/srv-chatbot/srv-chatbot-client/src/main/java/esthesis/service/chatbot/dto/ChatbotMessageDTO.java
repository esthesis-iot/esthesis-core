package esthesis.service.chatbot.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Data Transfer Object for chatbot messages.
 */
@Data
@Accessors(chain = true)
public class ChatbotMessageDTO {

	private String message;
	private String correlationId;
	private long timestamp;
	private boolean userInput;

}
