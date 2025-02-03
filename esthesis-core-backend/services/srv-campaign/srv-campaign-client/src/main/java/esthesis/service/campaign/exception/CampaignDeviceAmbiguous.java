package esthesis.service.campaign.exception;

import esthesis.common.exception.QMismatchException;

/**
 * Exception thrown when a device is ambiguous in a campaign (i.e. when multiple devices match the
 * same ID).
 */
@SuppressWarnings("java:S110")
public class CampaignDeviceAmbiguous extends QMismatchException {

	public CampaignDeviceAmbiguous(String message, Object... args) {
		super(message, args);
	}
}
