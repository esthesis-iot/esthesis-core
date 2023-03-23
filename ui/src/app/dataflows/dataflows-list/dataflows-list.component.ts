import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {MatSort} from "@angular/material/sort";
import {Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {DataflowsService} from "../dataflows.service";
import {DataflowDto} from "../dto/dataflow-dto";
import {dataflows} from "../dto/dataflow-definition";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-dataflows-list",
  templateUrl: "./dataflows-list.component.html"
})
export class DataflowsListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["name", "description", "type", "status"];
  dataSource: MatTableDataSource<DataflowDto> = new MatTableDataSource<DataflowDto>();
  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
    private dataflowService: DataflowsService, private qForms: QFormsService) {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW);
    this.filterForm = this.fb.group({
      name: [],
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
    this.dataflowService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [], false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      // Convert keys to text.
      onNext.content.forEach((dataflowDto: DataflowDto) => {
        // @ts-ignore
        dataflowDto.typeText = dataflows.find((dataflow) => {
          return dataflow.type === dataflowDto.type;
        }).title;
      });
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
}
