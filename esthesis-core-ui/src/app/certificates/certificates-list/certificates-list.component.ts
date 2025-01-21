import {AfterViewInit, Component, Input, OnInit, Optional, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {CertificatesService} from "../certificates.service";
import {CertificateDto} from "../dto/certificate-dto";
import {QFormsService} from "@qlack/forms";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {MatDialogRef} from "@angular/material/dialog";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-certificates-list",
  templateUrl: "./certificates-list.component.html"
})
export class CertificatesListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  @Input() embedded = false;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  columns: string[] = [];
  datasource = new MatTableDataSource<CertificateDto>();

  constructor(private readonly certificateService: CertificatesService,
    private readonly qForms: QFormsService,
    @Optional() private readonly dialogRef: MatDialogRef<CertificatesListComponent>,
    private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.CERTIFICATES);
  }

  ngOnInit(): void {
    this.columns = this.embedded
      ? ["name", "issued", "validity", "issuer"]
      : ["name", "cn", "issued", "validity", "issuer"];
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
    .subscribe({
      next: onNext => {
        this.datasource.data = onNext.content;
        this.paginator.length = onNext.totalElements;
      }, error: onError => {
        this.utilityService.popupErrorWithTraceId("Could not fetch certificates.", onError);
      }
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  embeddedClick(cert: CertificateDto) {
    this.dialogRef.close(cert);
  }
}
