import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {QFormsService} from "@qlack/forms";
import {CampaignsService} from "../campaigns.service";
import {CampaignDto} from "../dto/campaign-dto";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: "app-campaigns",
  templateUrl: "./campaigns.component.html",
  styleUrls: ["./campaigns.component.scss"]
})
export class CampaignsComponent extends BaseComponent implements AfterViewInit {
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  columns = ["name", "state", "createdOn", "startedOn", "terminatedOn"];
  datasource = new MatTableDataSource<CampaignDto>();

  constructor(private campaignsService: CampaignsService, private qForms: QFormsService) {
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
