import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";
import {QFormsService} from "@qlack/forms";
import {ProvisioningService} from "../provisioning.service";
import {TagService} from "../../tags/tag.service";
import {TagDto} from "../../dto/tag-dto";
import * as _ from "lodash";
import {BaseComponent} from "../../shared/component/base-component";
import {AppConstants} from "../../app.constants";
import {ProvisioningDto} from "../dto/provisioning-dto";
import {UtilityService} from "../../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: "app-provisioning-list",
  templateUrl: "./provisioning-list.component.html",
  styleUrls: ["./provisioning-list.component.scss"]
})
export class ProvisioningListComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ["name", "version", "prerequisiteVersion", "state", "size", "tags", "createdOn", "type", "cacheStatus"];
  datasource = new MatTableDataSource<ProvisioningDto>();
  availableTags: TagDto[] | undefined;
  // The list of all packages, so that base-version references can be resolved.
  availableProvisioningPackages: ProvisioningDto[] | undefined;
  // Expose application constants.
  constants = AppConstants;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private provisioningService: ProvisioningService, private qForms: QFormsService,
    private tagService: TagService, private utilityService: UtilityService,
    private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
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
    this.sort!.sortChange.subscribe((onNext: { active: string; direction: string; }) => {
      this.paginator!.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    this.provisioningService.find(
      this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator!.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort!.start);
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

  cacheAll() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Recache all packages",
        question: "Are you sure you want to download all packages and update them in Redis?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.provisioningService.recacheAll().subscribe({
          next: (next) => {
            this.utilityService.popupSuccess("Started caching provisioning packages..");
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not start caching provisioning packages.", error);
          }
        });
      }
    });
  }
}

