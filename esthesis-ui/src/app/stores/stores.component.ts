import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../shared/component/base-component';
import {MatTableDataSource} from '@angular/material/table';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {Router} from '@angular/router';
import {StoreDto} from '../dto/store-dto';
import {StoresService} from './stores.service';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import {QFormsService} from '@qlack/forms';

@Component({
  selector: 'app-stores',
  templateUrl: './stores.component.html',
  styleUrls: ['./stores.component.scss']
})
export class StoresComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ['name', 'createdOn'];
  datasource: MatTableDataSource<StoreDto> = new MatTableDataSource<StoreDto>();
  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, { static: true }) sort!: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
              private storesService: StoresService,
              private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      name: ['', null]
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(onNext => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
        this.sort.start);
    });
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
    // Convert FormGroup to a query string to pass as a filter.
    this.storesService.getAll(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      null!, false, page, size, sort, sortDirection))
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
