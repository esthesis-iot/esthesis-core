import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {FormBuilder, FormGroup} from "@angular/forms";
import {QFilterAlias, QFormsService} from "@qlack/forms";
import {UtilityService} from "../../../shared/services/utility.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {SecurityPoliciesService} from "../../security-policies.service";
import {PolicyDto} from "../../dto/policy-dto";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-security-policies-list",
  templateUrl: "./security-policies-list.component.html",
  styleUrls: []
})
export class SecurityPoliciesListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  displayedColumns = ["name", "description", "rule"];
  dataSource: MatTableDataSource<PolicyDto> = new MatTableDataSource<PolicyDto>();
  filterForm: FormGroup;

  constructor(private readonly fb: FormBuilder,
    private readonly securityPoliciesService: SecurityPoliciesService,
    private readonly qForms: QFormsService, private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.POLICIES);
    this.filterForm = this.fb.group({
      name: [],
      description: [],
      rule: [],
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
    this.securityPoliciesService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [new QFilterAlias('name', 'name*'), new QFilterAlias('description', 'description*'),
        new QFilterAlias('rule', 'rule*')], false, page, size, sort, sortDirection)).subscribe({
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
