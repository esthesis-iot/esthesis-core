package esthesis.platform.backend.server.config;

import esthesis.platform.backend.server.config.AppConstants.WebSocket;
import esthesis.platform.backend.server.security.TopicSubscriptionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /** Configure the prefix of inbound channels */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker(WebSocket.TOPIC_PREFIX);
  }

  /** Configure the broker */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOrigins("*");
  }

  /** Set an interceptor for inbound channels to apply security */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new TopicSubscriptionInterceptor());
  }
}
