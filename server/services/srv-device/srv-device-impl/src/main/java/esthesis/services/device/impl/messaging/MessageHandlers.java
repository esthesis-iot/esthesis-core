package esthesis.services.device.impl.messaging;

import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.messaging.TagServiceMessaging;
import esthesis.services.device.impl.repository.DeviceRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Slf4j
@ApplicationScoped
public class MessageHandlers {

  @Inject
  DeviceRepository deviceRepository;

  @Incoming(TagServiceMessaging.CHANNEL_DELETE)
  public void tagDeleted(Tag tag) {
    log.debug("Got tag deleted message: {}", tag);
    deviceRepository.find("tags", tag.getName()).stream()
        .forEach(device -> {
          device.getTags().removeIf(s -> s.equals(tag.getName()));
          deviceRepository.update(device);
        });
    log.debug("Tag removed from devices");
  }

}
