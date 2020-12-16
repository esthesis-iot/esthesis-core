import {Component, OnInit} from '@angular/core';
import {DataSource} from '@angular/cdk/collections';
import {AuditDto} from '../../dto/audit-dto';
import {BaseComponent} from '../../shared/component/base-component';

@Component({
  selector: 'app-dashboard-error-log',
  templateUrl: './dashboard-error-log.component.html',
  styleUrls: []
})
export class DashboardErrorLogComponent extends BaseComponent implements OnInit {

  displayedColumns: string[] = ['createdOn', 'shortDescription'];
  dataSource!: DataSource<AuditDto>;

  constructor() {
    super();
  }

  ngOnInit() {
  }

}
