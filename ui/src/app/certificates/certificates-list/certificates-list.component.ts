import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";
import {CertificatesService} from "../certificates.service";
import {CertificateDto} from "../dto/certificate-dto";
import {BaseComponent} from "../../shared/component/base-component";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-certificates-list",
  templateUrl: "./certificates-list.component.html",
  styleUrls: ["./certificates-list.component.scss"]
})
export class CertificatesListComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ["cn", "issued", "validity", "issuer"];
  datasource = new MatTableDataSource<CertificateDto>();
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private certificateService: CertificatesService, private qForms: QFormsService) {
    super();
  }

  ngOnInit() {
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
