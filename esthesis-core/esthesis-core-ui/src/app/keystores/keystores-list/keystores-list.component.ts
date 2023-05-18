import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {MatSort} from "@angular/material/sort";
import {Router} from "@angular/router";
import {KeystoresService} from "../keystores.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {QFormsService} from "@qlack/forms";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {KeystoreDto} from "../dto/keystore-dto";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-keystores-list",
  templateUrl: "./keystores-list.component.html",
  styleUrls: ["./keystores-list.component.scss"]
})
export class KeystoresListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  displayedColumns = ["name", "description", "createdOn"];
  datasource: MatTableDataSource<KeystoreDto> = new MatTableDataSource<KeystoreDto>();
  filterForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router,
    private storesService: KeystoresService, private qForms: QFormsService,
    private keystoresServices: KeystoresService, private utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.KEYSTORE);
    this.filterForm = this.fb.group({
      name: []
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
    this.keystoresServices.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      null!, false, page, size, sort, sortDirection))
    .subscribe({
      next: onNext => {
        this.datasource.data = onNext.content;
        this.paginator.length = onNext.totalElements;
      }, error: onError => {
        this.utilityService.popupErrorWithTraceId("Could not fetch keystores.", onError);
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
