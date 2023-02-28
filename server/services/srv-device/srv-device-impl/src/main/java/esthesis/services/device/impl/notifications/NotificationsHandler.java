package esthesis.services.device.impl.notifications;

import esthesis.common.AppConstants.MessagingKafka;
import esthesis.common.kafka.AppMessage;
import esthesis.services.device.impl.service.DeviceTagService;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.TracingMetadata;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class NotificationsHandler {

  @Inject
  DeviceTagService deviceTagService;

  private void handleTagDeleted(String tagId) {
    deviceTagService.removeTagById(tagId);
  }

  @Blocking
  @Incoming(MessagingKafka.SMALLRYE_KAFKA_CHANNEL + "-in")
  public CompletionStage<Void> onMessage(Message<AppMessage> msg) {
    log.trace("Processing Kafka application message '{}'", msg);
    Scope scope = Context.current().makeCurrent();

    try {
      Optional<TracingMetadata> tracingMetadata = msg.getMetadata().get(TracingMetadata.class);
      if (tracingMetadata.isPresent()) {
        scope = tracingMetadata.get().getCurrentContext().makeCurrent();
      }
      switch (msg.getPayload().getComponent()) {
        case TAG -> {
          switch (msg.getPayload().getAction()) {
            case DELETE -> handleTagDeleted(msg.getPayload().getId());
          }
        }
      }
    } catch (Exception e) {
      log.warn("Could not handle Kafka message '{}'.", msg, e);
    } finally {
      scope.close();
    }

    return msg.ack();
  }
}
