import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {ApplicationDto} from "../dto/application-dto";
import {ApplicationsService} from "../applications.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {BaseComponent} from "../../shared/components/base-component";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-applications-list",
  templateUrl: "./applications-list.component.html",
  styleUrls: ["./applications-list.component.scss"]
})
export class ApplicationsListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["name", "state", "createdOn"];
  dataSource: MatTableDataSource<ApplicationDto> = new MatTableDataSource<ApplicationDto>();
  filterForm: FormGroup;
  // Expose application constants.
  constants = AppConstants;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
    private applicationsService: ApplicationsService,
    private qForms: QFormsService) {
    super(AppConstants.SECURITY.CATEGORY.APPLICATION);
    this.filterForm = this.fb.group({
      name: [],
      state: [],
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
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
    this.applicationsService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      null!, false, page, size, sort, sortDirection))
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
