import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {CertificatesService} from "../certificates.service";
import {CertificateDto} from "../dto/certificate-dto";
import {BaseComponent} from "../../shared/component/base-component";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: "app-certificates-list",
  templateUrl: "./certificates-list.component.html",
  styleUrls: ["./certificates-list.component.scss"]
})
export class CertificatesListComponent extends BaseComponent implements AfterViewInit {
  columns = ["name", "cn", "issued", "validity", "issuer"];
  datasource = new MatTableDataSource<CertificateDto>();
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private certificateService: CertificatesService, private qForms: QFormsService) {
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
    this.certificateService.find(
      this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
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
