import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {FormBuilder, FormGroup} from '@angular/forms';
import 'rxjs/add/operator/debounceTime';
import * as moment from 'moment';
import {QFilterAlias, QFormsService} from '@eurodyn/forms';
import {AuditDto} from '../dto/audit-dto';
import {AuditService} from './audit.service';
import {UserService} from '../users/user.service';
import {KeyValueDto} from '../dto/key-value-dto';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent extends BaseComponent implements OnInit, AfterViewInit {
  // Columns to display.
  displayedColumns = ['createdOn', 'level', 'event', 'description'];

  // Datasource definition.
  datasource: MatTableDataSource<AuditDto> = new MatTableDataSource<AuditDto>();

  // Search filter.
  filterForm: FormGroup;
  auditEvents: KeyValueDto[];
  auditLevels: KeyValueDto[];

  // References to sorting and pagination.
  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private auditService: AuditService, private fb: FormBuilder,
              private userService: UserService, private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      dateFrom: ['', null],
      dateTo: ['', null],
      level: ['', null],
      event: ['', null],
      user_id: ['', null]
    });
  }

  ngOnInit() {
    this.auditService.getEvents().subscribe(onNext => {
      this.auditEvents = onNext;
    });
    this.auditService.getLevels().subscribe(onNext => {
      this.auditLevels = onNext;
    });

    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.debounceTime(500).subscribe(onNext => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
        this.sort.start);
    });
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Each time the sorting changes, reset the page number.
    this.sort.sortChange.subscribe(onNext => {
      this.paginator.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    // Since date selectors select a date in format yyyy-mm-dd and audit logs are kept as timestamps
    // we need to convert them to include a 24hrs span.
    if (this.filterForm.value['dateFrom'] && !this.filterForm.value['dateTo']) {
      this.filterForm.value['dateTo'] = moment().endOf('day');
    } else if (this.filterForm.value['dateTo'] && !this.filterForm.value['dateFrom']) {
      this.filterForm.value['dateFrom'] = moment(1);
      this.filterForm.value['dateTo'] = this.filterForm.value['dateTo'].endOf('day');
    } else if (this.filterForm.value['dateFrom'] && this.filterForm.value['dateTo']) {
      this.filterForm.value['dateFrom'] = this.filterForm.value['dateFrom'].startOf('day');
      this.filterForm.value['dateTo'] = this.filterForm.value['dateTo'].endOf('day');
    }

    // Convert FormGroup to a query string to pass as a filter.
    this.auditService.getLogs(this.qForms.makeQueryString(this.filterForm, [
      new QFilterAlias('dateFrom', 'createdOn'),
      new QFilterAlias('dateTo', 'createdOn')], false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  clearFilter() {
    this.filterForm.reset();
  }
}
