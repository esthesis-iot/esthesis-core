import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {DevicesService} from "../../devices.service";
import {ActivatedRoute} from "@angular/router";
import {DevicePageFieldDataDto} from "../../dto/device-page-field-data-dto";
import {MatSort} from "@angular/material/sort";
import * as _ from "lodash-es";
import {MatTableDataSource} from "@angular/material/table";
import {UtilityService} from "../../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-device-data",
  templateUrl: "./device-data.component.html",
  styleUrls: ["./device-data.component.scss"]
})
export class DeviceDataComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  id: string | null;
  // Columns to display.
  displayedColumns = ["measurement", "value", "lastUpdatedOn", "lastUpdatedOnAgo"];

  // Datasource definition.
  datasource: MatTableDataSource<DevicePageFieldDataDto> = new MatTableDataSource<DevicePageFieldDataDto>();
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private devicesService: DevicesService, private utilityService: UtilityService,
    private route: ActivatedRoute) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE, route.snapshot.paramMap.get("id"));
    this.id = this.route.snapshot.paramMap.get("id");
  }

  ngOnInit(): void {
    this.devicesService.getDeviceData(this.id!).subscribe({
      next: (data) => {
        this.datasource.data = _.sortBy(data, (item) => {
          return item.label;
        });
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch device data.", err);
      }
    });
  }

  ngAfterViewInit(): void {
    // Each time the sorting changes, reset the page number.
    this.sort.sortChange.subscribe((onNext: { active: string; direction: string; }) => {
      this.datasource.data = _.orderBy(this.datasource.data, (item) => {
        switch (onNext.active) {
          case "measurement":
            return item.label;
          case "value":
            return item.value;
          case "lastUpdatedOn":
            return item.lastUpdate;
          default:
            return item.label;
        }
      }, onNext.direction === "asc" ? "asc" : "desc");
    });
  }

  copyValue(row: any) {
    this.utilityService.copyToClipboard(row.value);
  }
}
