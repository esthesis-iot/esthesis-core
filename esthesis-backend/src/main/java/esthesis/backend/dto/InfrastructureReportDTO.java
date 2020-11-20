package esthesis.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InfrastructureReportDTO {
  private String nodeId;
  private boolean inClusterMode;
  private boolean clusterLeader;
  private List<MqttServerReport> mqttServerReports;
  private ZookeeperReport zookeeperReport;

  @Data
  @NoArgsConstructor
  @Accessors(chain = true)
  public static class MqttServerReport {
    private long id;
    private String ipAddress;
    private boolean leader;
  }

  @Data
  @NoArgsConstructor
  @Accessors(chain = true)
  public static class ZookeeperReport {
    private long id;
    private String ipAddress;
    private boolean leader;
  }
}
