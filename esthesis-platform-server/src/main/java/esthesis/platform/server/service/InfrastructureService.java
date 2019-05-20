package esthesis.platform.server.service;

import esthesis.platform.server.config.AppProperties;
import esthesis.platform.server.dto.InfrastructureReportDTO;
import esthesis.platform.server.dto.InfrastructureReportDTO.ZookeeperReport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfrastructureService {

  private final AppProperties appProperties;
  private final ZookeeperService zookeeperService;
  private final MQTTService mqttService;

  public InfrastructureService(AppProperties appProperties, ZookeeperService zookeeperServerService,
      MQTTService mqttServerService) {
    this.appProperties = appProperties;
    this.zookeeperService = zookeeperServerService;
    this.mqttService = mqttServerService;
  }

  private List<InfrastructureReportDTO.MqttServerReport> getMqttServersReport() {
//    return mqttService.findActive().stream().map(mqttServerDTO ->
//        new InfrastructureReportDTO.MqttServerReport()
//            .setId(mqttServerDTO.getId())
//            .setIpAddress(mqttServerDTO.getIpAddress())
//            .setLeader(mqttService.isLeader(mqttServerDTO.getId()))
//    ).collect(Collectors.toList());
    return null;
  }

  private ZookeeperReport getZookeeperReport() {
//    return new ZookeeperReport()
//        .setIpAddress(zookeeperService.getConnectionString())
//        .setLeader(zookeeperService.isLeader());
    return null;
  }

  public InfrastructureReportDTO getReport() {
//    return new InfrastructureReportDTO()
//        .setNodeId(appProperties.getNodeId())
//        .setInClusterMode(zookeeperService.findActive().fileSize() > 0)
//        .setClusterLeader(zookeeperService.isLeader())
//        .setMqttServerReports(getMqttServersReport())
//        .setZookeeperReport(getZookeeperReport());
    return null;
  }
}
