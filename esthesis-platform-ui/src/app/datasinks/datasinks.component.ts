import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {QFormsService} from '@eurodyn/forms';
import {DataSinkDto} from '../dto/data-sink-dto';
import {DataSinkService} from './data-sink.service';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-datasinks',
  templateUrl: './datasinks.component.html',
  styleUrls: ['./datasinks.component.scss']
})
export class DatasinksComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['name', 'createdOn', 'state', 'metadataWrite', 'telemetryWrite',
    'metadataRead', 'telemetryRead'];
  datasource = new MatTableDataSource<DataSinkDto>();

  @ViewChild(MatSort, { static: true }) sort!: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator!: MatPaginator;

  constructor(private dataSinkService: DataSinkService, private qForms: QFormsService) {
    super();
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Each time the sorting changes, reset the page number.
    this.sort.sortChange.subscribe((onNext: { active: string; direction: string; }) => {
      this.paginator.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    this.dataSinkService.getAll(
      this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

}
