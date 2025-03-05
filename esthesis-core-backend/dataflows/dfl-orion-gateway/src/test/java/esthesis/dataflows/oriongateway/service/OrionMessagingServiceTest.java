package esthesis.dataflows.oriongateway.service;

import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrionMessagingServiceTest {

	@Mock
	Exchange exchange;

	@Mock
	Message message;

	@Mock
	AppConfig appConfig;

	@Mock
	OrionGatewayService orionGatewayService;

	AppMessage appMessage;

	@InjectMocks
	OrionMessagingService orionMessagingService;

	@BeforeEach
	void setUp() {
		// Initialize mocks.
		MockitoAnnotations.openMocks(this);
		appMessage = new AppMessage();

		// Set up common mock behaviors.
		when(exchange.getIn()).thenReturn(message);
		when(message.getBody(AppMessage.class)).thenReturn(appMessage);


	}

	@Test
	void onAppMessage_Create_OK() {
		// Enable device creation in the configuration.
		when(appConfig.orionCreateDevice()).thenReturn(true);

		// Set up the app message for creating a device.
		appMessage.setComponent(Component.DEVICE);
		appMessage.setSubject(Subject.DEVICE);
		appMessage.setAction(Action.CREATE);
		appMessage.setTargetId("deviceId");

		// Act: Call the method to handle the app message.
		orionMessagingService.onAppMessage(exchange);

		// Verify that the device registration request was executed.
		verify(orionGatewayService, times(1)).registerDeviceOnOrion("deviceId");
	}

	@Test
	void onAppMessage_Create_NOK() {
		// Disable device creation in the configuration.
		when(appConfig.orionCreateDevice()).thenReturn(false);

		// Set up the app message for creating a device.
		appMessage.setComponent(Component.DEVICE);
		appMessage.setSubject(Subject.DEVICE);
		appMessage.setAction(Action.CREATE);
		appMessage.setTargetId("deviceId");

		// Act: Call the method to handle the app message.
		orionMessagingService.onAppMessage(exchange);

		// Verify that the device registration request was not executed.
		verify(orionGatewayService, times(0)).registerDeviceOnOrion("deviceId");
	}

	@Test
	void onAppMessage_Delete_OK() {
		// Enable device deletion in the configuration.
		when(appConfig.orionDeleteDevice()).thenReturn(true);

		// Set up the app message for deleting a device.
		appMessage.setComponent(Component.DEVICE);
		appMessage.setSubject(Subject.DEVICE);
		appMessage.setAction(Action.DELETE);
		appMessage.setTargetId("deviceId");

		// Act: Call the method to handle the app message.
		orionMessagingService.onAppMessage(exchange);

		// Verify that the device deletion request was executed.
		verify(orionGatewayService, times(1)).deleteEntityByEsthesisId("deviceId");
	}

	@Test
	void onAppMessage_Delete_NOK() {
		// Disable device deletion in the configuration.
		when(appConfig.orionDeleteDevice()).thenReturn(false);

		// Set up the app message for deleting a device.
		appMessage.setComponent(Component.DEVICE);
		appMessage.setSubject(Subject.DEVICE);
		appMessage.setAction(Action.DELETE);
		appMessage.setTargetId("deviceId");

		// Act: Call the method to handle the app message.
		orionMessagingService.onAppMessage(exchange);

		// Verify that the device deletion request was not executed.
		verify(orionGatewayService, times(0)).deleteEntityByEsthesisId("deviceId");
	}

	@Test
	void onAppMessage_Update_OK() {
		// Enable device attribute update in the configuration.
		when(appConfig.orionUpdateData()).thenReturn(true);

		// Set up the app message for updating a device attribute.
		appMessage.setComponent(Component.DEVICE);
		appMessage.setSubject(Subject.DEVICE_ATTRIBUTE);
		appMessage.setAction(Action.UPDATE);
		appMessage.setTargetId("deviceId");

		// Act: Call the method to handle the app message.
		orionMessagingService.onAppMessage(exchange);

		// Verify that the device update request was executed.
		verify(orionGatewayService, times(1)).syncAttributes("deviceId");
	}

	@Test
	void onAppMessage_Update_NOK() {
		// Disable device attribute update in the configuration.
		when(appConfig.orionUpdateData()).thenReturn(false);

		// Set up the app message for updating a device attribute.
		appMessage.setComponent(Component.DEVICE);
		appMessage.setSubject(Subject.DEVICE_ATTRIBUTE);
		appMessage.setAction(Action.UPDATE);
		appMessage.setTargetId("deviceId");

		// Act: Call the method to handle the app message.
		orionMessagingService.onAppMessage(exchange);

		// Verify that the device update request was not executed.
		verify(orionGatewayService, times(0)).syncAttributes("deviceId");
	}


}
