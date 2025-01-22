import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {QFormsService} from "@qlack/forms";
import {CampaignsService} from "../campaigns.service";
import {CampaignDto} from "../dto/campaign-dto";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-campaigns",
  templateUrl: "./campaigns.component.html"
})
export class CampaignsComponent extends SecurityBaseComponent implements AfterViewInit {
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  columns = ["name", "state", "createdOn", "startedOn", "terminatedOn"];
  datasource = new MatTableDataSource<CampaignDto>();

  constructor(private readonly campaignsService: CampaignsService,
    private readonly qForms: QFormsService) {
    super(AppConstants.SECURITY.CATEGORY.CAMPAIGN);
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
    this.campaignsService.find(
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
