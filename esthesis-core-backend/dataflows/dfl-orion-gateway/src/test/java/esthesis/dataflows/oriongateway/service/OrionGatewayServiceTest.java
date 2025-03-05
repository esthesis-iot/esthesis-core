package esthesis.dataflows.oriongateway.service;

import esthesis.common.avro.PayloadData;
import esthesis.common.avro.ValueData;
import esthesis.common.avro.ValueTypeEnum;
import esthesis.common.data.DataUtils.ValueType;
import esthesis.dataflows.oriongateway.TestHelper;
import esthesis.dataflows.oriongateway.config.AppConfig;
import esthesis.service.device.resource.DeviceSystemResource;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static esthesis.dataflows.oriongateway.service.OrionClientService.ATTRIBUTE_TYPE.TELEMETRY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrionGatewayServiceTest {

	@InjectMocks
	OrionGatewayService orionGatewayService;

	@Mock
	DeviceSystemResource deviceSystemResource;

	@Mock
	OrionClientService orionClientService;

	@Mock
	AppConfig appConfig;

	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testHelper = new TestHelper();

	}

	@Test
	void addExistingEsthesisDevicesToOrion() {

		// Mock existing devices in the system.
		when(deviceSystemResource.getDeviceIds()).thenReturn(List.of("device-id-1", "device-id-2"));

		// Mock finding the devices attribute including one enabling the registration of devices in Orion.
		when(deviceSystemResource.getDeviceAttributesByEsthesisId(anyString()))
			.thenReturn(
				List.of(
					testHelper.createDeviceAttributeEntity(
						"device-id",
						"test-attribute",
						"test-value",
						ValueType.STRING),
					testHelper.createDeviceAttributeEntity(
						"device-id",
						"enable-registration",
						"true",
						ValueType.BOOLEAN)));

		// Mock finding the devices by esthesis ID.
		when(deviceSystemResource.findById(anyString()))
			.thenReturn(testHelper.createDeviceEntity(anyString()));

		// Mock orion entity not existing.
		when(orionClientService.getEntityByOrionId("urn.test:test-hardware-id")).thenReturn(null);

		// Perform the add existing devices' operation.
		orionGatewayService.addExistingEsthesisDevicesToOrion();

		// Verifying that the create operation was called once per device in the Orion client service.
		verify(orionClientService, times(2)).createEntity(any());

	}

	@Test
	void registerDeviceOnOrion() {
		// Mock default configurations for the Orion Gateway.
		when(appConfig.orionDefaultType()).thenReturn("test-type");
		when(appConfig.orionIdPrefix()).thenReturn("urn.test:");
		when(appConfig.attributeEsthesisId()).thenReturn("esthesisId");
		when(appConfig.attributeEsthesisHardwareId()).thenReturn("esthesisHardwareId");
		when(appConfig.orionRegistrationEnabledAttribute()).thenReturn(Optional.of("enable-registration"));

		// Mock finding the devices attribute including one enabling the registration of devices in Orion.
		when(deviceSystemResource.getDeviceAttributesByEsthesisId(anyString()))
			.thenReturn(
				List.of(
					testHelper.createDeviceAttributeEntity(
						"device-id",
						"test-attribute",
						"test-value",
						ValueType.STRING),
					testHelper.createDeviceAttributeEntity(
						"device-id",
						"enable-registration",
						"true",
						ValueType.BOOLEAN)));

		// Mock finding a device entity by esthesis ID.
		when(deviceSystemResource.findById(anyString()))
			.thenReturn(testHelper.createDeviceEntity("test-hardware-id"));

		// Mock orion entity not existing.
		when(orionClientService.getEntityByOrionId("urn.test:test-hardware-id")).thenReturn(null);

		// Perform the registration operation.
		orionGatewayService.registerDeviceOnOrion("esthesis-id");

		// Verifying that the create operation was called in the Orion client service.
		verify(orionClientService, times(1)).createEntity(any());
	}

	@Test
	void syncAttributes() {
		// Mock default configurations for the Orion Gateway.
		when(appConfig.orionUpdateDataAttribute()).thenReturn(Optional.empty());
		when(appConfig.orionIdPrefix()).thenReturn("urn.test:");
		when(appConfig.attributeEsthesisId()).thenReturn("esthesisId");
		when(appConfig.orionRegistrationEnabledAttribute()).thenReturn(Optional.of("enable-registration"));

		// Mock finding the devices attribute including one enabling the registration of devices in Orion.
		when(deviceSystemResource.getDeviceAttributesByEsthesisId(anyString()))
			.thenReturn(
				List.of(
					testHelper.createDeviceAttributeEntity(
						"device-id",
						"test-attribute",
						"test-value",
						ValueType.STRING),
					testHelper.createDeviceAttributeEntity(
						"device-id",
						"enable-registration",
						"true",
						ValueType.BOOLEAN)));

		// Mock finding a device entity by hardware ID.
		when(deviceSystemResource.findById(anyString()))
			.thenReturn(testHelper.createDeviceEntity("test-hardware-id"));

		// Mock finding an Orion entity by the given hardware ID and prefix.
		when(orionClientService.getEntityByOrionId(anyString()))
			.thenReturn(testHelper.createOrionEntity("urn.test:test-hardware-id"));

		// Mock orion client update attributes request.
		doNothing().when(orionClientService).setAttribute(anyString(), anyString(), anyString(), any(), any());

		// Perform the sync attributes' operation.
		orionGatewayService.syncAttributes("esthesis-id");

		// Verifying that the update operation was called in the Orion client only once for the "test-attribute" attribute.
		// The "enable-registration" attribute should not be sent as it is a configuration attribute.
		verify(orionClientService, times(1)).setAttribute(anyString(), anyString(), anyString(), any(), any());
	}

	@Test
	void deleteEntityByEsthesisId() {
		// Mock finding a device entity by hardware ID.
		when(deviceSystemResource.findById(anyString()))
			.thenReturn(testHelper.createDeviceEntity("test-hardware-id"));

		// Mock finding an Orion entity by the given hardware ID and prefix.
		when(orionClientService.getEntityByOrionId(anyString()))
			.thenReturn(testHelper.createOrionEntity("urn.test:test-hardware-id"));

		// Perform the delete operation.
		orionGatewayService.deleteEntityByEsthesisId("test-hardware-id");

		// Verifying that the delete operation was called in the Orion client service.
		verify(orionClientService, times(1)).deleteEntity(anyString());

	}

	@Test
	@DisplayName("Process data")
	void processData() {
		// Mock default configurations for the Orion Gateway.
		when(appConfig.orionUpdateData()).thenReturn(true);
		when(appConfig.orionUpdateDataAttribute()).thenReturn(Optional.empty());
		when(appConfig.orionIdPrefix()).thenReturn("urn.test:");


		// Mock camel exchange message parameter
		Exchange exchangeMock = Mockito.mock(Exchange.class);
		Message messageMock = Mockito.mock(Message.class);
		when(exchangeMock.getIn()).thenReturn(messageMock);

		// Mock esthesis data message.
		when(messageMock.getBody(any()))
			.thenReturn(testHelper.createEsthesisDataMessage(
					"hardware-id",
					new PayloadData(
						"category",
						Instant.now().toString(),
						List.of(new ValueData("value-name", "value", ValueTypeEnum.STRING)))
				)
			);

		// Mock orion entity as already registered in Orion.
		when(orionClientService.getEntityByOrionId(anyString()))
			.thenReturn(testHelper.createOrionEntity("urn.test:hardware-id"));

		// Mock sending data to Orion client.
		doNothing().when(orionClientService).setAttribute(anyString(), anyString(), anyString(), any(), any());


		// Perform the process data operation.
		orionGatewayService.processData(exchangeMock);

		// Verifying that the set attribute operation was called in the Orion client service with the correct parameters.
		verify(orionClientService, times(1))
			.setAttribute(
				"urn.test:hardware-id",
				"category.value-name",
				"value",
				ValueType.STRING,
				TELEMETRY);

		// Assert save or update entities was not called.
		verify(orionClientService, times(0)).saveOrUpdateEntities(anyString());
	}


	@Test
	@DisplayName("Process data with custom JSON from device attribute")
	void processDataWithCustomJsonDevice() {
		// Mock default configurations for the Orion Gateway.
		when(appConfig.orionUpdateData()).thenReturn(true);
		when(appConfig.orionUpdateDataAttribute()).thenReturn(Optional.empty());

		// Mock camel exchange message parameter
		Exchange exchangeMock = Mockito.mock(Exchange.class);
		Message messageMock = Mockito.mock(Message.class);
		when(exchangeMock.getIn()).thenReturn(messageMock);


		String timeISOFormat = "2025-02-07T23:59:59Z";

		// Mock esthesis data message.
		when(messageMock.getBody(any()))
			.thenReturn(testHelper.createEsthesisDataMessage(
					"hardware-id",
					new PayloadData(
						"category",
						timeISOFormat,
						List.of(new ValueData("value-name", "value", ValueTypeEnum.STRING)))
				)
			);

		// Mock the existence of the custom JSON attribute name
		when(appConfig.orionCustomEntityJsonFormatAttributeName()).thenReturn(Optional.of("custom-json"));

		// Prepare a custom JSON template and its expected output for the device attribute.
		String quteTemplateJsonInput = "{\"custom-level\":\"device\",\"name\":\"{measurementName}\",\"value\":\"{measurementValue}\"," +
			"\"category\":\"{category}\",\"hardwareId\":\"{hardwareId}\",\"timestamp\":\"{timestamp}\"}";

		String quteTemplateExpectedOutput = "{\"custom-level\":\"device\",\"name\":\"value-name\",\"value\":\"value\"," +
			"\"category\":\"category\",\"hardwareId\":\"hardware-id\",\"timestamp\":\"2025-02-07T23:59:59Z\"}";

		// Mock custom json attribute value.
		when(deviceSystemResource.getDeviceAttributeByEsthesisHardwareIdAndAttributeName("hardware-id", "custom-json"))
			.thenReturn(Optional.of(
				testHelper.createDeviceAttributeEntity(
					"hardware-id",
					"custom-json",
					quteTemplateJsonInput,
					ValueType.STRING
				)
			));

		// Mock sending data to Orion client.
		doNothing().when(orionClientService).saveOrUpdateEntities(anyString());


		// Perform the process data operation.
		orionGatewayService.processData(exchangeMock);

		// Verifying that save or update entities was called in the Orion client service with the correct parameter.
		verify(orionClientService, times(1)).saveOrUpdateEntities(quteTemplateExpectedOutput);

		// Assert set attributes was not called.
		verify(orionClientService, times(0))
			.setAttribute(anyString(), anyString(), anyString(), any(), any());
	}


	@Test
	@DisplayName("Process data with custom JSON from dataflow environment variable")
	void processDataWithCustomJsonDataflow() {
		// Mock default configurations for the Orion Gateway.
		when(appConfig.orionUpdateData()).thenReturn(true);
		when(appConfig.orionUpdateDataAttribute()).thenReturn(Optional.empty());

		// Mock camel exchange message parameter
		Exchange exchangeMock = Mockito.mock(Exchange.class);
		Message messageMock = Mockito.mock(Message.class);
		when(exchangeMock.getIn()).thenReturn(messageMock);


		String timeISOFormat = "2025-02-07T23:59:59Z";

		// Mock esthesis data message.
		when(messageMock.getBody(any()))
			.thenReturn(testHelper.createEsthesisDataMessage(
					"hardware-id",
					new PayloadData(
						"category",
						timeISOFormat,
						List.of(new ValueData("value-name", "value", ValueTypeEnum.STRING)))
				)
			);

		// Prepare a custom JSON  Qute template.
		String quteTemplateJsonInput = "{\"custom-level\":\"dataflow\",\"name\":\"{measurementName}\",\"value\":\"{measurementValue}\"," +
			"\"category\":\"{category}\",\"hardwareId\":\"{hardwareId}\",\"timestamp\":\"{timestamp}\"}";

		// Mock the existence of the custom JSON environment variable with the Qute template input.
		when(appConfig.orionCustomEntityJsonFormat()).thenReturn(Optional.of(quteTemplateJsonInput));

		// Expected output for the custom JSON Qute template.
		String quteTemplateExpectedOutput = "{\"custom-level\":\"dataflow\",\"name\":\"value-name\",\"value\":\"value\"," +
			"\"category\":\"category\",\"hardwareId\":\"hardware-id\",\"timestamp\":\"2025-02-07T23:59:59Z\"}";


		// Mock sending data to Orion client.
		doNothing().when(orionClientService).saveOrUpdateEntities(anyString());


		// Perform the process data operation.
		orionGatewayService.processData(exchangeMock);

		// Verifying that save or update entities was called in the Orion client service with the correct parameter.
		verify(orionClientService, times(1)).saveOrUpdateEntities(quteTemplateExpectedOutput);

		// Assert set attributes was not called.
		verify(orionClientService, times(0))
			.setAttribute(anyString(), anyString(), anyString(), any(), any());
	}

	@Test
	void isDataUpdateAllowed() {
		// Mock default configurations for the Orion Gateway.
		when(appConfig.orionUpdateData()).thenReturn(true);
		when(appConfig.orionUpdateDataAttribute()).thenReturn(Optional.empty());


		// Mock orion entity as already registered in Orion.
		when(orionClientService.getEntityByOrionId(anyString()))
			.thenReturn(testHelper.createOrionEntity("urn.test:hardware-id"));

		// Assert that data update is allowed.
		assertTrue(orionGatewayService.isDataUpdateAllowed("hardware-id"));
	}
}
