import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-dashboard-error-log',
  templateUrl: './dashboard-error-log.component.html',
  styleUrls: []
})
export class DashboardErrorLogComponent implements OnInit {

  displayedColumns: string[] = ['createdOn', 'shortDescription'];
  dataSource;
  // : AuditDto[] = [
  //   {id: '1', createdOn: new Date(), event: '', shortDescription: 'Could not connect to remote MQTT.', level: ''},
  //   {id: '2', createdOn: new Date(), event: '', shortDescription: 'Device sent unparsable data.', level: ''},
  //   {id: '3', createdOn: new Date(), event: '', shortDescription: 'Could not connect to remote MQTT.', level: ''},
  //   {id: '4', createdOn: new Date(), event: '', shortDescription: 'Device sent unparsable data.', level: ''},
  //   {id: '5', createdOn: new Date(), event: '', shortDescription: 'Could not connect to remote MQTT.', level: ''},
  //   {id: '6', createdOn: new Date(), event: '', shortDescription: 'Device sent unparsable data.', level: ''},
  // ];

  constructor() { }

  ngOnInit() {
  }

}
