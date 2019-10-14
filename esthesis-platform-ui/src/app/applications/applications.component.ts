import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {ApplicationDto} from '../dto/application-dto';
import {ApplicationService} from './application.service';
import {QFormsService} from '@eurodyn/forms';
import 'rxjs/add/operator/debounceTime';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-applications',
  templateUrl: './applications.component.html',
  styleUrls: ['./applications.component.scss']
})
export class ApplicationsComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ['name', 'state', 'createdOn'];
  dataSource: MatTableDataSource<ApplicationDto> = new MatTableDataSource<ApplicationDto>();
  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
              private applicationsService: ApplicationService,
              private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      name: ['', null],
      state: ['', null],
    });
  }

  ngOnInit() {
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
    // Convert FormGroup to a query string to pass as a filter.
    this.applicationsService.getAll(this.qForms.makeQueryString(this.filterForm,
      null, false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.dataSource.data = onNext.content;
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
