package esthesis.services.settings.impl.notifications;

import esthesis.service.settings.entity.SettingEntity;
import esthesis.services.settings.impl.service.SettingsService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationsHandlerTest {

	@InjectMocks
	NotificationsHandler notificationsHandler;

	@Mock
	SettingsService settingsService;

	@Mock
	Message<AppMessage> message;

	@Mock
	Metadata metadata;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Mock the behavior of the metadata and TracingMetadata.
		TracingMetadata tracingMetadata = Mockito.mock(TracingMetadata.class);
		when(tracingMetadata.getCurrentContext()).thenReturn(Mockito.mock(Context.class));
		when(metadata.get(TracingMetadata.class)).thenReturn(Optional.of(tracingMetadata));
		when(message.getMetadata()).thenReturn(metadata);
	}


	@Test
	void onMessage() {
		// Mock the behavior of the Setting service.
		when(settingsService.findByName(any())).thenReturn(new SettingEntity("root-ca-test", "test-ca-id"));
		when(settingsService.saveUpdate(any())).thenReturn(new SettingEntity());

		// Mock a message payload.
		AppMessage appMessage = AppMessage.builder()
			.component(KafkaNotificationsConstants.Component.CA)
			.action(KafkaNotificationsConstants.Action.DELETE)
			.targetId("test-ca-id")
			.build();

		when(message.getPayload()).thenReturn(appMessage);

		assertDoesNotThrow(() -> notificationsHandler.onMessage(message));

		verify(settingsService).saveUpdate(any());

	}
}
