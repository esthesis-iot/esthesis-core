package esthesis.service.tag.messaging;

import esthesis.common.AppConstants;

public class TagServiceMessaging {

  private TagServiceMessaging() {
  }

  public static final String TOPIC_TAG_DELETE =
      AppConstants.KAFKA_TOPIC_PREFIX + "tag-delete";
}
