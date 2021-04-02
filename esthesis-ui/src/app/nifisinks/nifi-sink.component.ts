import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {QFormsService} from '@qlack/forms';
import {NifiSinkService} from './nifi-sink.service';
import {BaseComponent} from '../shared/component/base-component';
import {ActivatedRoute, Router} from '@angular/router';
import {NiFiSinkDto} from '../dto/nifisinks/nifi-sink-dto';
import {MatTableDataSource} from '@angular/material/table';
import {AppConstants} from "../app.constants";

@Component({
  selector: 'app-nifisink',
  templateUrl: './nifi-sink.component.html',
  styleUrls: ['./nifi-sink.component.scss']
})
export class NiFiSinkComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['name', 'createdOn', 'handler', 'state', 'validationErrors'];
  datasource = new MatTableDataSource<NiFiSinkDto>();
  type: string | undefined;
  activeNiFiId = localStorage.getItem('activeNiFi');
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private nifiSinkService: NifiSinkService,
              private qForms: QFormsService,
              private route: ActivatedRoute,
              private router: Router) {
    super();
  }

  ngOnInit() {
    this.type = this.router.url.replace("/", "");
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
    this.nifiSinkService.getAll(
      this.qForms.appendPagingToFilter("type=" + this.type, page, size, sort, sortDirection))
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
