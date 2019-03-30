package esthesis.platform.server.service;

import esthesis.platform.server.config.AppConstants.WebSocket;
import esthesis.platform.server.dto.WebSocketMessageDTO;
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
