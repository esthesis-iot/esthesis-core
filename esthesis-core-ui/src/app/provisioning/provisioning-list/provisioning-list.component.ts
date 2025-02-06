import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {QFormsService} from "@qlack/forms";
import {ProvisioningService} from "../provisioning.service";
import {AppConstants} from "../../app.constants";
import {ProvisioningDto} from "../dto/provisioning-dto";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-provisioning-list",
  templateUrl: "./provisioning-list.component.html"
})
export class ProvisioningListComponent extends SecurityBaseComponent implements AfterViewInit {
  columns = ["name", "version", "state", "size", "createdOn", "type"];
  datasource = new MatTableDataSource<ProvisioningDto>();
  // The list of all packages, so that base-version references can be resolved.
  availableProvisioningPackages: ProvisioningDto[] | undefined;
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private readonly provisioningService: ProvisioningService,
    private readonly qForms: QFormsService, private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.PROVISIONING);
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
    this.provisioningService.find(
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

  refreshData() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.direction);
  }

}

