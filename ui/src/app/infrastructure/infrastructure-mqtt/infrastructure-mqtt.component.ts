import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {QFormsService, QPageableReply} from "@qlack/forms";
import {InfrastructureMqttService} from "./infrastructure-mqtt.service";
import {BaseComponent} from "../../shared/component/base-component";
import {InfrastructureMqttDto} from "./dto/Infrastructure-mqtt-dto";
import {UtilityService} from "../../shared/service/utility.service";
import * as _ from "lodash";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: "app-infrastructure-mqtt",
  templateUrl: "./infrastructure-mqtt.component.html",
  styleUrls: ["./infrastructure-mqtt.component.scss"]
})
export class InfrastructureMqttComponent extends BaseComponent implements AfterViewInit {
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  columns = ["name", "url", "tags", "active"];
  datasource = new MatTableDataSource<InfrastructureMqttDto>();
  availableTags: TagDto[] | undefined;

  constructor(private infrastructureService: InfrastructureMqttService,
    private qForms: QFormsService, private utilityService: UtilityService,
    private tagService: TagsService) {
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

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe({
      next: (next) => {
        this.availableTags = next.content;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "Could not fetch tags, please try again later.", error);
      }
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

  resolveTag(ids: string[]): string {
    return _.map(
      _.filter(this.availableTags, i => _.includes(ids, i.id)),
      (j) => {
        return j.name;
      }
    ).join(", ");
  }
}
