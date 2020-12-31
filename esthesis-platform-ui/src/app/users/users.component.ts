import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {UserDto} from '../dto/user-dto';
import {UserService} from './user.service';
import 'rxjs/add/operator/debounceTime';
import {BaseComponent} from '../shared/component/base-component';
import {QFilterAlias, QFormsService} from '@qlack/forms';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ['email', 'status'];
  dataSource: MatTableDataSource<UserDto> = new MatTableDataSource<UserDto>();

  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, { static: true }) sort!: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router, private userService: UserService,
              private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      username: ['', null],
      status: ['', null],
    });
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

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.debounceTime(500).subscribe(onNext => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
        this.sort.start);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    // Convert FormGroup to a query string to pass as a filter.
    this.userService.getAll(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [new QFilterAlias('type', 'userType')], false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.dataSource.data = onNext.content;
      this.paginator!.length = onNext.totalElements;
      console.log(onNext); // TODO REMOVE before commit
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
