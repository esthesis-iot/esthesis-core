import { Component, OnInit } from '@angular/core';
import {DeviceDto} from '../../dto/device-dto';

@Component({
  selector: 'app-dashboard-new-registrations',
  templateUrl: './dashboard-new-registrations.component.html',
  styleUrls: ['./dashboard-new-registrations.component.scss']
})
export class DashboardNewRegistrationsComponent implements OnInit {

  displayedColumns: string[] = ['id', 'device', 'lastSeen'];
  dataSource: DeviceDto[] = [
    {id: 'few12f3', device: 'Develco Z344-D', lastSeen: new Date()},
    {id: '24fs235', device: 'Develco Z344-D', lastSeen: new Date()},
    {id: 'ffe4424', device: 'Develco Z344-D', lastSeen: new Date()},
    {id: '1242243', device: 'Develco Z344-D', lastSeen: new Date()},
    {id: '12fsfg3', device: 'Develco Z344-D', lastSeen: new Date()}
  ];

  constructor() { }

  ngOnInit() {
  }

}
