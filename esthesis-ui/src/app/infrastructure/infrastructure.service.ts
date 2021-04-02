import {Injectable} from '@angular/core';
import {CrudService} from '../services/crud.service';
import {InfrastructureReportDto} from '../dto/infrastructure-report-dto';
import {HttpClient} from '@angular/common/http';
import {AppConstants} from '../app.constants';

@Injectable({
  providedIn: 'root'
})
export class InfrastructureService extends CrudService<InfrastructureReportDto> {

  constructor(http: HttpClient) {
    super(http, 'infrastructure');
  }

  releaseZookeeperLeadership() {
    return this.http.delete(`${AppConstants.API_ROOT}/infrastructure/rl/zookeeper`);
  }

  releaseMqttLeadership(mqttServerId: number) {
    return this.http.delete(`${AppConstants.API_ROOT}/infrastructure/rl/mqtt/${mqttServerId}`);
  }
}
