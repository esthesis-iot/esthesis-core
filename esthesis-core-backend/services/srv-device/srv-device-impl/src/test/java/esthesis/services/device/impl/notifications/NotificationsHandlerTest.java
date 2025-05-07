package esthesis.services.device.impl.notifications;

import esthesis.services.device.impl.service.DeviceTagService;
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationsHandlerTest {

	@InjectMocks
	NotificationsHandler notificationsHandler;

	@Mock
	DeviceTagService deviceTagService;

	@Mock
	Message<AppMessage> message;

	@Mock
	Metadata metadata;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Mock the behavior of the DeviceTagService.
		doNothing().when(deviceTagService).removeTagById(anyString());

		// Mock the behavior of the metadata and TracingMetadata.
		TracingMetadata tracingMetadata = Mockito.mock(TracingMetadata.class);
		when(tracingMetadata.getCurrentContext()).thenReturn(Mockito.mock(Context.class));
		when(metadata.get(TracingMetadata.class)).thenReturn(Optional.of(tracingMetadata));
		when(message.getMetadata()).thenReturn(metadata);
	}


	@Test
	void onMessage() {
		// Mock a message payload.
		AppMessage appMessage = AppMessage.builder()
			.component(KafkaNotificationsConstants.Component.TAG)
			.action(KafkaNotificationsConstants.Action.DELETE)
			.targetId("test-tag-id")
			.build();

		when(message.getPayload()).thenReturn(appMessage);

		assertDoesNotThrow(() -> notificationsHandler.onMessage(message));
		verify(deviceTagService).removeTagById(anyString());
	}

	@Test
	void onMessageWithInvalidAction() {
		// Mock the behavior of the message payload.
		AppMessage appMessage = AppMessage.builder()
			.component(KafkaNotificationsConstants.Component.TAG)
			.action(KafkaNotificationsConstants.Action.CREATE)
			.targetId("test-tag-id")
			.build();

		when(message.getPayload()).thenReturn(appMessage);

		assertDoesNotThrow(() -> notificationsHandler.onMessage(message));
		verify(deviceTagService, Mockito.times(0)).removeTagById(anyString());
	}
}
