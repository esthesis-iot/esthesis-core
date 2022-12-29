import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {CaDto} from "../dto/ca-dto";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";
import {QFormsService} from "@qlack/forms";
import {CasService} from "../cas.service";
import {BaseComponent} from "../../shared/component/base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-cas-list",
  templateUrl: "./cas-list.component.html",
  styleUrls: ["./cas-list.component.scss"]
})
export class CasListComponent extends BaseComponent implements AfterViewInit {
  columns = ["name", "cn", "parent", "issued", "validity"];
  datasource = new MatTableDataSource<CaDto>();
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private caService: CasService, private qForms: QFormsService) {
    super();
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
    this.caService.find(this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

}
