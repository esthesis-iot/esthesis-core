import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {AuditDto} from "../dto/audit-dto";
import {AuditService} from "../audit.service";
import {QFilterAlias, QFormsService} from "@qlack/forms";
import * as _ from "lodash-es";
import moment from "moment";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-audit-list",
  templateUrl: "./audit-list.component.html"
})
export class AuditListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  // Columns to display.
  displayedColumns = ["createdOn", "createdBy", "category", "operation", "message"];

  // Datasource definition.
  datasource: MatTableDataSource<AuditDto> = new MatTableDataSource<AuditDto>();

  // Search filter.
  filterForm: FormGroup;
  auditCategories: string[] | undefined;
  auditOperations: string[] | undefined;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private readonly auditService: AuditService, private readonly fb: FormBuilder,
    private readonly qForms: QFormsService, private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.AUDIT);
    this.filterForm = this.fb.group({
      dateFrom: [],
      dateTo: [],
      operation: [],
      category: [],
      createdBy: []
    });
  }

  ngOnInit() {
    this.auditService.getCategories().subscribe({
      next: (categories: string[]) => {
        this.auditCategories = _.sortBy(categories);
      }, error: (onError: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch audit categories.", onError);
      }
    });
    this.auditService.getOperations().subscribe({
      next: (operations: string[]) => {
        this.auditOperations = _.sortBy(operations);
      }, error: (onError: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch audit operations.", onError);
      }
    });

    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active, this.sort.start);
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
    // Since date selectors select a date in format yyyy-mm-dd and audit logs are kept as timestamps
    // we need to convert them to include a 24hrs span.
    if (this.filterForm.value.dateFrom && !this.filterForm.value.dateTo) {
      this.filterForm.value.dateTo = moment().endOf("day");
    } else if (this.filterForm.value.dateTo && !this.filterForm.value.dateFrom) {
      this.filterForm.value.dateFrom = moment(1);
      this.filterForm.value.dateTo = this.filterForm.value.dateTo.endOf("day");
    } else if (this.filterForm.value.dateFrom && this.filterForm.value.dateTo) {
      this.filterForm.value.dateFrom = this.filterForm.value.dateFrom.startOf("day");
      this.filterForm.value.dateTo = this.filterForm.value.dateTo.endOf("day");
    }

    // Convert FormGroup to a query string to pass as a filter.
    this.auditService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(), [
      new QFilterAlias("dateFrom", "createdOn>="),
      new QFilterAlias("dateTo", "createdOn<=")], false, page, size, sort, sortDirection))
    .subscribe({
      next: (reply: any) => {
        this.datasource.data = reply.content;
        this.paginator.length = reply.totalElements;
      }, error: (onError: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch audit logs.", onError);
      }
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
