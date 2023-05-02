package esthesis.util.kafka.notifications.common;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
