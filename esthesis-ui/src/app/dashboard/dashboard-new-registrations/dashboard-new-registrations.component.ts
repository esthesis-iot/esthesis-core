import { Component, OnInit } from '@angular/core';
import {DeviceDto} from '../../dto/device-dto';
import {BaseComponent} from '../../shared/component/base-component';

@Component({
  selector: 'app-dashboard-new-registrations',
  templateUrl: './dashboard-new-registrations.component.html',
  styleUrls: []
})
export class DashboardNewRegistrationsComponent extends BaseComponent implements OnInit {

  displayedColumns: string[] = ['id', 'device', 'lastSeen'];
  dataSource: DeviceDto[] = [
    {id: 'few12f3', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'},
    {id: '24fs235', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'},
    {id: 'ffe4424', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'},
    {id: '1242243', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'},
    {id: '12fsfg3', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'}
  ];

  constructor() {
    super();
  }

  ngOnInit() {
  }

}
