package esthesis.platform.server.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;
import java.util.logging.Logger;

//TODO STOMP security
public class TopicSubscriptionInterceptor implements ChannelInterceptor {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(TopicSubscriptionInterceptor.class.getName());

  private boolean validateSubscription(Principal principal, String topicDestination) {
    if (principal == null) {
//      LOGGER.log(Level.FINE, "Principal is null.");
      // unauthenticated user
      return false;
    }
//    LOGGER.log(Level.FINE, "Validate subscription for {0} to topic {1}.",
//        new Object[]{principal.getName(), topicDestination});
    //Additional validation logic coming here
    return true;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
//    System.out.println("COMMAND: " + headerAccessor.getCommand());
    if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
      Principal userPrincipal = headerAccessor.getUser();
      if (!validateSubscription(userPrincipal, headerAccessor.getDestination())) {
        throw new IllegalArgumentException("No permission for this topic");
      }
    }
    return message;
  }

}


