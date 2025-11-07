package esthesis.services.chatbot.impl.tools;

import esthesis.service.device.resource.DeviceResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test class for DeviceTools, testing chatbot device tools functionality.
 */
class DeviceToolsTest {

	@InjectMocks
	DeviceTools deviceTools;

	@Mock
	DeviceResource deviceResource;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void devicesStatusNOK() {
		Long result = deviceTools.devicesStatus("INVALID_STATUS");
		assertEquals(0L, result);
	}

	@Test
	void devicesStatusOK() {
		when(deviceResource.countByStatus(any())).thenReturn(5L);
		Long result = deviceTools.devicesStatus("REGISTERED");
		assertEquals(5L, result);
	}

}
