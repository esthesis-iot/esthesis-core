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
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DeviceTextDataImportDto} from "../../dto/device-text-data-import-dto";
import {DeviceDataImportDto} from "../../dto/device-data-import-dto";

@Component({
  selector: "app-device-data",
  templateUrl: "./device-data.component.html"
})
export class DeviceDataComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  id: string | null;
  form!: FormGroup;
  elpFile: File | null = null;
  displayedColumns = ["measurement", "value", "lastUpdatedOn", "lastUpdatedOnAgo"];
  tableLoading = true;
  datasource: MatTableDataSource<DevicePageFieldDataDto> = new MatTableDataSource<DevicePageFieldDataDto>();
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private readonly devicesService: DevicesService,
    private readonly utilityService: UtilityService,
    private readonly route: ActivatedRoute, private readonly fb: FormBuilder) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE, route.snapshot.paramMap.get("id"));
    this.id = this.route.snapshot.paramMap.get("id");
  }

  ngOnInit(): void {
    // Fetch device data.
    this.devicesService.getDeviceData(this.id!).subscribe({
      next: (data) => {
        this.datasource.data = _.sortBy(data, (item) => {
          return item.label;
        });
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch device data.", err);
      }, complete: () => {
        this.tableLoading = false;
      }
    });

    // Prepare the form to manually enter data.
    this.form = this.fb.group({
      data: [null],
      batchSize: [null, [Validators.pattern("^[0-9]*$")]],
      batchDelay: [null, [Validators.pattern("^[0-9]*$")]],
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

  importDeviceData(dataImportType: string, dataImportSource: string) {
    // Shared reply handler.
    const replyHandler = {
      next: () => {
        this.utilityService.popupSuccess("Data imported successfully.");
        this.clearTextData();
        this.clearFileData();
      }, error: (err: any) => {
        this.utilityService.popupErrorWithTraceId("Could not import data.", err);
      }
    };

    if (dataImportSource == this.appConstants.DEVICE.DATA_IMPORT.SOURCE.TEXT) {
      this.devicesService.importDeviceDataFromText(dataImportType, this.id!,
        this.form.getRawValue() as DeviceTextDataImportDto).subscribe(replyHandler);
    } else if (dataImportSource == this.appConstants.DEVICE.DATA_IMPORT.SOURCE.FILE) {
      this.devicesService.importDeviceDataFromFile(dataImportType, this.id!,
        this.form.getRawValue() as DeviceDataImportDto, this.elpFile!).subscribe(replyHandler);
    } else {
      this.utilityService.popupError("Invalid data import source.");
    }
  }

  clearTextData() {
    this.form.reset();
  }

  clearFileData() {
    const fileInput = document.getElementById("file") as HTMLInputElement;
    if (fileInput) {
      fileInput.value = "";
    }
  }

  selectELPFile(event: any) {
    const file: File = event.target?.files[0];
    if (file) {
      this.elpFile = file;
    }
  }
}
