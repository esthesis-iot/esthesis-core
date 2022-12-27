import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {DevicesService} from "../../devices.service";
import {UtilityService} from "../../../shared/service/utility.service";
import {ActivatedRoute} from "@angular/router";
import {MatTableDataSource} from "@angular/material/table";
import {DevicePageFieldDataDto} from "../../dto/device-page-field-data-dto";
import {MatSort} from "@angular/material/sort";
import * as _ from "lodash-es";

@Component({
  selector: "app-device-data",
  templateUrl: "./device-data.component.html",
  styleUrls: ["./device-data.component.scss"]
})
export class DeviceDataComponent implements OnInit, AfterViewInit {
  id: string | null;
  // Columns to display.
  displayedColumns = ["measurement", "value", "lastUpdatedOn", "lastUpdatedOnAgo"];

  // Datasource definition.
  datasource: MatTableDataSource<DevicePageFieldDataDto> = new MatTableDataSource<DevicePageFieldDataDto>();
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private devicesService: DevicesService, private utilityService: UtilityService,
    private route: ActivatedRoute) {
    this.id = this.route.snapshot.paramMap.get("id");
  }

  ngOnInit(): void {
    this.devicesService.getAllDeviceData(this.id!).subscribe({
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
      this.datasource.data = _.sortBy(this.datasource.data, (item) => {
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
      });

      if (onNext.direction === "desc") {
        this.datasource.data.reverse();
      }
    });
  }

  copyValue(row: any) {
    this.utilityService.copyToClipboard(row.value);
  }
}
