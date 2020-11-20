package esthesis.backend.nifi.client.dto;

import lombok.Data;

@Data
public class CallReplyDTO {

  private boolean isSuccessful;
  private String body;
  private int code;
}
