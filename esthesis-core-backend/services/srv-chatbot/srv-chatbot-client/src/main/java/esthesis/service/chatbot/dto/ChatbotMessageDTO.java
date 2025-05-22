package esthesis.service.chatbot.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatbotMessageDTO {

	private String message;
	private String correlationId;
	private long timestamp;
	private boolean userInput;

}
