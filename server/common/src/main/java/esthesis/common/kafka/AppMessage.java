package esthesis.common.kafka;

import static esthesis.common.AppConstants.MessagingKafka.Action;
import static esthesis.common.AppConstants.MessagingKafka.Component;
import static esthesis.common.AppConstants.MessagingKafka.Subject;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMessage {

  @NotNull
  private Component component;

  @NotNull
  private Subject subject;

  @NotNull
  private Action action;

  private String id;

  private String payload;

}
