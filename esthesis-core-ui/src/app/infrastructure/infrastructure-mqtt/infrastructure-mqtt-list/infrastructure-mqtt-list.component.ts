import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {InfrastructureMqttDto} from "../dto/Infrastructure-mqtt-dto";
import {TagDto} from "../../../tags/dto/tag-dto";
import {InfrastructureMqttService} from "../infrastructure-mqtt.service";
import {QFormsService, QPageableReply} from "@qlack/forms";
import {UtilityService} from "../../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-infrastructure-mqtt-list",
  templateUrl: "./infrastructure-mqtt-list.component.html"
})
export class InfrastructureMqttListComponent extends SecurityBaseComponent implements AfterViewInit {
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  columns = ["name", "url", "active"];
  datasource = new MatTableDataSource<InfrastructureMqttDto>();
  availableTags: TagDto[] | undefined;

  constructor(private readonly infrastructureService: InfrastructureMqttService,
    private readonly qForms: QFormsService, private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.INFRASTRUCTURE);
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
    this.infrastructureService.find(
      this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
    .subscribe({
      next: (mqttServers: QPageableReply<InfrastructureMqttDto>) => {
        this.datasource.data = mqttServers.content;
        this.paginator.length = mqttServers.totalElements;
      }, error: (error: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch MQTT servers.", error);
      }
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }
}
