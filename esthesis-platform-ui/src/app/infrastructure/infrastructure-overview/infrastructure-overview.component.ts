import { Component, OnInit } from '@angular/core';
import {InfrastructureService} from '../infrastructure.service';
import {InfrastructureReportDto} from '../../dto/infrastructure-report-dto';

@Component({
  selector: 'app-infrastructure-overview',
  templateUrl: './infrastructure-overview.component.html',
  styleUrls: ['./infrastructure-overview.component.scss']
})
export class InfrastructureOverviewComponent implements OnInit {
  report: InfrastructureReportDto;

  constructor(private infrastructureService: InfrastructureService) { }

  private getReport() {
    this.infrastructureService.getFirst().subscribe(onNext => {
      this.report = onNext;
    });
  }

  ngOnInit() {
    this.getReport();
  }

  releaseZookeeperLeadership() {
    this.infrastructureService.releaseZookeeperLeadership().subscribe(onNext => {
      this.getReport();
    } );
  }

  releaseMqttLeadership(mqttServerId: number) {
    this.infrastructureService.releaseMqttLeadership(mqttServerId).subscribe(onNext => {
      this.getReport();
    } );
  }
}
