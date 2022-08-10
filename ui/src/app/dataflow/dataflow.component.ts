import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {BaseComponent} from "../shared/component/base-component";
import {MatTableDataSource} from "@angular/material/table";
import {FormBuilder, FormGroup} from "@angular/forms";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {DataflowService} from "./dataflow.service";
import {DataflowDto} from "../dto/dataflow/dataflow-dto";

@Component({
  selector: "app-dataflow",
  templateUrl: "./dataflow.component.html",
  styleUrls: ["./dataflow.component.scss"]
})
export class DataflowComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["name"];
  dataSource: MatTableDataSource<DataflowDto> = new MatTableDataSource<DataflowDto>();
  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
    private dataflowService: DataflowService, private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      name: ["", null],
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
    this.dataflowService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [], false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.dataSource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  clearFilter() {
    this.filterForm.reset();
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  resolveLink(dataflowDto: DataflowDto): string {
    switch (dataflowDto.type) {
      case this.appConstants.DATAFLOW_TYPE.MQTT_CLIENT:
        return "mqtt-client/" + dataflowDto.id;
      default:
        return dataflowDto.id;
    }
  }
}
