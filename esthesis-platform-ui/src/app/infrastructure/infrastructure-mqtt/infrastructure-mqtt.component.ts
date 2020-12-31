import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {QFormsService} from '@qlack/forms';
import {MqttServerDto} from '../../dto/mqtt-server-dto';
import {MqttServerService} from './mqtt-server.service';
import {BaseComponent} from '../../shared/component/base-component';

@Component({
  selector: 'app-infrastructure-mqtt',
  templateUrl: './infrastructure-mqtt.component.html',
  styleUrls: ['./infrastructure-mqtt.component.scss']
})
export class InfrastructureMqttComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['name', 'ipAddress', 'status'];
  datasource = new MatTableDataSource<MqttServerDto>();

  @ViewChild(MatSort, { static: true }) sort!: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator!: MatPaginator;

  constructor(private mqttServerService: MqttServerService, private qForms: QFormsService) {
    super();
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Each time the sorting changes, reset the page number.
    this.sort!.sortChange.subscribe((onNext: { active: string; direction: string; }) => {
      this.paginator!.pageIndex = 0;
      this.fetchData(0, this.paginator!.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    this.mqttServerService.getAll(
      this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator!.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort!.start);
  }

}
