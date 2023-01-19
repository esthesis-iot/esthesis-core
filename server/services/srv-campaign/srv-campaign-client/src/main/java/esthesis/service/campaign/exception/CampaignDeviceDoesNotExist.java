package esthesis.service.campaign.exception;

import esthesis.common.exception.QDoesNotExistException;

public class CampaignDeviceDoesNotExist extends QDoesNotExistException {

  public CampaignDeviceDoesNotExist(String message, Object... args) {
    super(message, args);
  }
}
