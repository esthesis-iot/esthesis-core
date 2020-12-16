export interface InfrastructureReportDto {
  clusterLeader: boolean;
  inClusterMode: boolean;
  nodeId: string;
  mqttServerReports: MqttServerReport[];
  zookeeperReport: ZookeeperReport;
}

interface MqttServerReport {
  id: number;
  ipAddress: string;
  leader: boolean;
}

interface ZookeeperReport {
  id: number;
  ipAddress: string;
  leader: boolean;
}
