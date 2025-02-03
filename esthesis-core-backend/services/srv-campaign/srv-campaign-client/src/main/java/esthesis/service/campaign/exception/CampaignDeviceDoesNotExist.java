package esthesis.service.campaign.exception;

import esthesis.common.exception.QDoesNotExistException;

/**
 * Exception thrown when a device does not exist in a campaign.
 */
@SuppressWarnings("java:S110")
public class CampaignDeviceDoesNotExist extends QDoesNotExistException {

	public CampaignDeviceDoesNotExist(String message, Object... args) {
		super(message, args);
	}
}
