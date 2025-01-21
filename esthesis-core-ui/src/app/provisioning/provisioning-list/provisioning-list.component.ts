import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {QFormsService} from "@qlack/forms";
import {ProvisioningService} from "../provisioning.service";
import {TagsService} from "../../tags/tags.service";
import {TagDto} from "../../tags/dto/tag-dto";
import * as _ from "lodash-es";
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
  columns = ["name", "version", "prerequisiteVersion", "state", "size", "tags", "createdOn", "type"];
  datasource = new MatTableDataSource<ProvisioningDto>();
  availableTags: TagDto[] | undefined;
  // The list of all packages, so that base-version references can be resolved.
  availableProvisioningPackages: ProvisioningDto[] | undefined;
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private readonly provisioningService: ProvisioningService,
    private readonly qForms: QFormsService,
    private readonly tagService: TagsService, private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.PROVISIONING);
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe({
      next: (next) => {
        this.availableTags = next.content;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "Could not fetch tags, please try again later.", error);
      }
    });

    // Get available provisioning packages.
    this.provisioningService.find("sort=name,asc").subscribe({
      next: (next) => {
        this.availableProvisioningPackages = next.content;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "Could not fetch provisioning packages, please try again later.", error);
      }
    });

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

  resolveTag(ids: string[]): string {
    return _.map(
      _.filter(this.availableTags, i => _.includes(ids, i.id)),
      (j) => {
        return j.name;
      }
    ).join(", ");
  }

  resolveBaseVersion(ids: string[]): string {
    return _.map(
      _.filter(this.availableProvisioningPackages, i => _.includes(ids, i.id)),
      (j) => {
        return j.name + " - " + j.version;
      }
    ).join(", ");
  }

  refreshData() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.direction);
  }

}

