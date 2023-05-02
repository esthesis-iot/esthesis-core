package esthesis.dataflows.oriongateway.service;

import esthesis.dataflows.oriongateway.config.AppConfig;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class OrionSchedulerService {

	@Inject
	AppConfig appConfig;

	@Inject
	OrionGatewayService orionGatewayService;

	/**
	 * A scheduled task that checks if existing devices should be added to Orion.
	 */
	@Scheduled(cron = "{esthesis.dfl.orion-retro-create-devices-schedule}")
	void checkIfExistingDevicesShouldBeAddedOnSchedule() {
		if (appConfig.orionRetroCreateDevicesOnSchedule()) {
			orionGatewayService.addExistingEsthesisDevicesToOrion();
		}
	}
}
