package esthesis.services.device.impl.messaging;

import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.messaging.TagServiceMessaging;
import esthesis.services.device.impl.service.DeviceService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
public class MessageHandlers {

  @Inject
  DeviceService deviceService;

  @Incoming(TagServiceMessaging.CHANNEL_DELETE)
  public void tagDeleted(Tag tag) {
    log.debug("Got tag deleted message: {}", tag);
    deviceService.removeTag(tag.getName());
    log.debug("Tag removed from devices");
  }
}
