package esthesis.services.chatbot.impl.tools;

import static esthesis.core.common.AppConstants.Device;

import dev.langchain4j.agent.tool.Tool;
import esthesis.service.device.resource.DeviceResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DeviceTools {

	@Inject
	@RestClient
	DeviceResource deviceResource;

	@Tool("Find the number of devices assigned to a specific status.")
//	public Uni<Long> devicesStatus(String status) {
	public Long devicesStatus(String status) {
		try {
			log.debug("Running deviceStatus method with status '{}'.", status);
			status = status.toUpperCase();
			return deviceResource.countByStatus(Device.Status.valueOf(status));
		} catch (Exception e) {
			log.error("Could not execute tool devicesStatus.", e);
			return 0L;
		}
	}
}
