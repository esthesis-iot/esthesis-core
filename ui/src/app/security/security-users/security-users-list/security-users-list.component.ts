import {AfterViewInit, Component, Input, OnInit, Optional, ViewChild} from "@angular/core";
import {BaseComponent} from "../../../shared/components/base-component";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {MatDialogRef} from "@angular/material/dialog";
import {UtilityService} from "../../../shared/services/utility.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {SecurityUsersService} from "../../security-users.service";
import {UserDto} from "../../dto/user-dto";

@Component({
  selector: "app-security-users-list",
  templateUrl: "./security-users-list.component.html",
  styleUrls: []
})
export class SecurityUsersListComponent extends BaseComponent implements OnInit, AfterViewInit {
  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  displayedColumns = ["username", "firstName", "lastName"];
  dataSource: MatTableDataSource<UserDto> = new MatTableDataSource<UserDto>();
  filterForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router, private securityUsersService: SecurityUsersService,
    private qForms: QFormsService, private utilityService: UtilityService) {
    super();
    this.filterForm = this.fb.group({
      username: [],
      firstName: [],
      lastName: [],
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500), distinctUntilChanged()
    ).subscribe({
      next: () => {
        this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
          this.sort.start);
      }
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
    this.securityUsersService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [], false, page, size, sort, sortDirection)).subscribe({
      next: (onNext) => {
        this.dataSource.data = onNext.content;
        this.paginator.length = onNext.totalElements;
      }, error: (onError: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch users.", onError);
      }
    });
  }

  clearFilter() {
    this.filterForm.reset();
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

}

