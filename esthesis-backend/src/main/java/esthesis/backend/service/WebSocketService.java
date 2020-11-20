package esthesis.backend.service;

import esthesis.backend.config.AppConstants.WebSocket;
import esthesis.backend.dto.WebSocketMessageDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketService {

  private final SimpMessagingTemplate msgTemplate;

  public WebSocketService(SimpMessagingTemplate template) {
    this.msgTemplate = template;
  }

  public void publish(WebSocketMessageDTO webSocketMessageDTO) {
    msgTemplate.convertAndSend(WebSocket.TOPIC_PREFIX + "/" + webSocketMessageDTO.getTopic(),
        webSocketMessageDTO.getPayload());
  }

}
