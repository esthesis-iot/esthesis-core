export class InfrastructureReportDto {
  clusterLeader: boolean;
  inClusterMode: boolean;
  nodeId: string;
  mqttServerReports: MqttServerReport[];
  zookeeperReport: ZookeeperReport;
}

class MqttServerReport {
  id: number;
  ipAddress: string;
  leader: boolean;
}

class ZookeeperReport {
  id: number;
  ipAddress: string;
  leader: boolean;
}
