import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {MatSort} from "@angular/material/sort";
import {UserDto} from "../dto/user-dto";
import {UserService} from "../user.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {BaseComponent} from "../../shared/components/base-component";
import {QFilterAlias, QFormsService} from "@qlack/forms";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";

@Component({
  selector: "app-users-list",
  templateUrl: "./users-list.component.html",
  styleUrls: ["./users-list.component.scss"]
})
export class UsersListComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["email", "status"];
  dataSource: MatTableDataSource<UserDto> = new MatTableDataSource<UserDto>();

  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router, private userService: UserService,
    private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      username: [],
      status: [],
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

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe({
      next: () => {
        this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
          this.sort.start);
      }
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    // Convert FormGroup to a query string to pass as a filter.
    this.userService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [new QFilterAlias("type", "userType")], false, page, size, sort, sortDirection))
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
