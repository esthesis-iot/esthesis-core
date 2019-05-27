import { Component, OnInit } from '@angular/core';
import {DeviceDto} from '../../dto/device-dto';
import {BaseComponent} from '../../shared/base-component';

@Component({
  selector: 'app-dashboard-inactive-devices',
  templateUrl: './dashboard-inactive-devices.component.html',
  styleUrls: ['./dashboard-inactive-devices.component.scss']
})
export class DashboardInactiveDevicesComponent extends BaseComponent implements OnInit {

  displayedColumns: string[] = ['id', 'device', 'lastSeen'];
  dataSource: DeviceDto[] = [
    {id: 'l32kfid', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'},
    {id: 'dfj3495', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'},
    {id: 'f3d3324', device: 'Develco Z344-D', lastSeen: new Date(), hardwareId: '123'}
  ];

  constructor() {
    super();
  }

  ngOnInit() {
  }

}
