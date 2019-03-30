package esthesis.platform.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ContainerDTO {
  private String image;
  private String registryUsername;
  private String registryPassword;
  private String network;
  private String restart;
  private List<ContainerPortDTO> ports;
  private List<ContainerVolumeDTO> volumes;
  private List<ContainerEnvDTO> env;
  private int server;
  private String name;

  // The number of replicas.
  private int scale;

  // The WebSocket topic id to send progress messages.
  private String wsId;
}
