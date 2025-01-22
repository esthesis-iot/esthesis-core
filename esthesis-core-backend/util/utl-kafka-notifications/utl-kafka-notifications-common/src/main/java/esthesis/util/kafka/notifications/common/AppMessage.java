package esthesis.util.kafka.notifications.common;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines an Application Message to be exchanged between application components using Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMessage {

	// The component this message is generated for.
	@NotNull
	private Component component;

	// The subject of the component.
	@NotNull
	private Subject subject;

	// The action that took place.
	@NotNull
	private Action action;

	// A unique ID for this message.
	@NotNull
	private String msgId;

	// If this messages targets a specific object, the ID of that object can be specified here.
	private String targetId;

	// A comment to be included with the message.
	private String comment;

	// If this message was sent to a broadcast topic.
	private boolean broadcast = false;
}
