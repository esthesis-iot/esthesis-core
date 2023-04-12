package esthesis.service.campaign.exception;

import esthesis.common.exception.QMismatchException;

public class CampaignDeviceAmbiguous extends QMismatchException {

  public CampaignDeviceAmbiguous(String message, Object... args) {
    super(message, args);
  }
}
