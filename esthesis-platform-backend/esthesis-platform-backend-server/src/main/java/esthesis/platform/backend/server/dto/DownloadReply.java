package esthesis.platform.backend.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DownloadReply {
  private String filename;
  private String payload;
  private byte[] binaryPayload;
}
