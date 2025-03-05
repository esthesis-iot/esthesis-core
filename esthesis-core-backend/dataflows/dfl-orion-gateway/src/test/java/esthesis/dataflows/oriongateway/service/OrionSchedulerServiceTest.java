package esthesis.dataflows.oriongateway.service;

import esthesis.dataflows.oriongateway.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrionSchedulerServiceTest {

	@Mock
	AppConfig appConfig;

	@Mock
	OrionGatewayService orionGatewayService;

	@InjectMocks
	OrionSchedulerService orionSchedulerService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		doNothing().when(orionGatewayService).addExistingEsthesisDevicesToOrion();
		when(appConfig.orionRetroCreateDevicesOnSchedule()).thenReturn(true);
	}

	@Test
	void checkIfExistingDevicesShouldBeAddedOnSchedule() {
		orionSchedulerService.checkIfExistingDevicesShouldBeAddedOnSchedule();
		verify(orionGatewayService).addExistingEsthesisDevicesToOrion();
	}
}
