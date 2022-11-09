package esthesis.services.device.impl.messaging;

import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.messaging.TagServiceMessaging;
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
public class MessageHandlers {

  @Inject
  DeviceTagService deviceTagService;

  @Blocking
  @Incoming(TagServiceMessaging.TOPIC_TAG_DELETE)
  public CompletionStage<Void> tagDeleted(Message<Tag> msg) {
    Scope scope = Context.current().makeCurrent();

    try {
      Optional<TracingMetadata> tracingMetadata = msg.getMetadata()
          .get(TracingMetadata.class);
      if (tracingMetadata.isPresent()) {
        scope = tracingMetadata.get().getCurrentContext().makeCurrent();
      }
      log.debug("Got a tag deleted message '{}'", msg.getPayload());
      deviceTagService.removeTag(msg.getPayload().getName());
    } finally {
      scope.close();
    }

    return msg.ack();
  }
}
