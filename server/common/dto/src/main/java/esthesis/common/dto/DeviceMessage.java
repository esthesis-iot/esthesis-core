package esthesis.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DeviceMessage<T> {

  // The hardware ID of the device. As this value is not signed, in non-trusted environments make
  // sure that you trust the device(s) sending you their IDs by having a signed payload.
  @NotNull
  @Getter
  @Setter
  @JsonProperty("i")
  private String hardwareId;

  // An optional signature for the content of this request.
  @Getter
  @Setter
  @JsonProperty("s")
  private String signature;

  // The payload of this message.
  @Getter
  @Setter
  @JsonProperty("p")
  private T payload;

  // The encrypted payload of this message.
  @Getter
  @Setter
  @JsonProperty("ep")
  private String encryptedPayload;

  public DeviceMessage(@NotNull String hardwareId) {
    this.hardwareId = hardwareId;
  }
}
