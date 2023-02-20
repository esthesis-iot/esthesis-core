import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {MatSort} from "@angular/material/sort";
import {DeviceDto} from "../dto/device-dto";
import {DevicesService} from "../devices.service";
import {BaseComponent} from "../../shared/components/base-component";
import {QFormsService} from "@qlack/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {CountdownComponent} from "ngx-countdown";

@Component({
  selector: "app-devices",
  templateUrl: "./devices.component.html",
  styleUrls: ["./devices.component.scss"]
})
export class DevicesComponent extends BaseComponent implements OnInit, AfterViewInit {
  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild("countdown", {static: false}) private countdown!: CountdownComponent;

  // Columns to display.
  displayedColumns = ["hardwareId", "lastSeen", "createdOn", "registeredOn", "status"];
  // Datasource definition.
  datasource: MatTableDataSource<DeviceDto> = new MatTableDataSource<DeviceDto>();
  // Search filter.
  filterForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router,
    private deviceService: DevicesService, private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      hardwareId: [],
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
    this.deviceService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(), [],
      false, page, size, sort, sortDirection))
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
