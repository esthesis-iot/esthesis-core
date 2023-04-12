import {AfterViewInit, Component, Input, OnInit, Optional, ViewChild} from "@angular/core";
import {CaDto} from "../dto/ca-dto";
import {MatSort} from "@angular/material/sort";
import {QFormsService} from "@qlack/forms";
import {CasService} from "../cas.service";
import {BaseComponent} from "../../shared/components/base-component";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {MatDialogRef} from "@angular/material/dialog";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-cas-list",
  templateUrl: "./cas-list.component.html"
})
export class CasListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  @Input() embedded = false;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  columns: string[] = [];
  datasource = new MatTableDataSource<CaDto>();

  constructor(private caService: CasService, private qForms: QFormsService,
    @Optional() private dialogRef: MatDialogRef<CasListComponent>) {
    super(AppConstants.SECURITY.CATEGORY.CA);
  }

  ngOnInit() {
    this.columns = this.embedded
      ? ["name", "cn", "parent", "issued", "validity"]
      : ["name", "cn", "parent", "issued", "validity"];
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

  embeddedClick(ca: CaDto) {
    this.dialogRef.close(ca);
  }
}
