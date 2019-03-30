import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {QFilterAlias, QFormsService} from '@eurodyn/forms';
import {UserDto} from '../dto/user-dto';
import {UserService} from './user.service';
import 'rxjs/add/operator/debounceTime';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit, AfterViewInit {
  displayedColumns = [ 'email', 'fn', 'ln', 'type', 'status', 'createdOn'];
  dataSource: MatTableDataSource<UserDto> = new MatTableDataSource<UserDto>();

  filterForm: FormGroup;
  userTypes: string[];
  userStatus: string[];

  // References to sorting and pagination.
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router, private userService: UserService, private qForms: QFormsService) {
    this.filterForm = this.fb.group({
      fn: ['', null],
      ln: ['', null],
      type: ['', null],
      status: ['', null],
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

  ngOnInit() {
    // Fetch distinct event types and levels to populate filter.
    this.userService.getUserRoles().subscribe(onNext => {
      this.userTypes = onNext;
    });
    this.userService.getUserStatus().subscribe(onNext => {
      this.userStatus = onNext;
    });

    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.debounceTime(500).subscribe(onNext => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active, this.sort.start);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    // Convert FormGroup to a query string to pass as a filter.
    this.userService.getAll(this.qForms.makeQueryString(this.filterForm,
      [new QFilterAlias('type', 'userType')], false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.dataSource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
      console.log(onNext); // TODO REMOVE before commit
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active, this.sort.start);
  }

  clearFilter() {
    this.filterForm.reset();
  }
}
