import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {DeviceDto} from "../../../devices/dto/device-dto";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {DevicesService} from "../../../devices/devices.service";
import {SettingsService} from "../../../settings/settings.service";
import {UtilityService} from "../../../shared/services/utility.service";
import {DashboardItemSensorConfigurationDto} from "../../dto/configuration/dashboard-item-sensor-configuration-dto";
import {QPageableReply} from "@qlack/forms";

@Component({
  selector: "app-dashboard-item-sensor-edit",
  templateUrl: "./dashboard-item-sensor-edit.component.html"
})
export class DashboardItemSensorEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  // A helper auto-complete container for devices matching the user's search input.
  searchHardwareIds?: string[];
  // A helper auto-complete container for measurement names.
  allUniqueMeasurements?: string[];
  filteredUniqueMeasurements?: string[];

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    private readonly fb: FormBuilder, private readonly deviceService: DevicesService,
    private readonly settingsService: SettingsService, private readonly utilityService: UtilityService,
    public dialogRef: MatDialogRef<DashboardItemSensorEditComponent>) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD);
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemSensorConfigurationDto;
    }

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_hardwareId: [configuration?.hardwareId, [Validators.required]],
      configuration_measurement: [configuration?.measurement, [Validators.required]],
      configuration_unit: [configuration?.unit],
      configuration_icon: [configuration?.icon],
      configuration_precision: [configuration?.precision, [Validators.pattern('^[0-9]+$')]],
      configuration_sparkline: [configuration?.sparkline],
      configuration_sparkline_points: [configuration?.sparklinePoints, [Validators.pattern('^[0-9]+$')]],
      configuration_threshold: [configuration?.threshold],
      configuration_threshold_low: [configuration?.thresholdLow],
      configuration_threshold_low_color: [configuration?.thresholdLowColor],
      configuration_threshold_middle: [configuration?.thresholdMiddle],
      configuration_threshold_middle_color: [configuration?.thresholdMiddleColor],
      configuration_threshold_high: [configuration?.thresholdHigh],
      configuration_threshold_high_color: [configuration?.thresholdHighColor],
    });

    // Fetch possible device measurements.
    this.settingsService.findMeasurementNames().subscribe({
      next: next => {
        this.allUniqueMeasurements = next;
        this.filteredUniqueMeasurements = next;
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Error fetching device measurements.", err);
      }
    });

    // Monitor for changes in search by hardware id input.
    this.form.get("configuration_hardwareId")!.valueChanges.pipe(
      debounceTime(300), distinctUntilChanged()
    ).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.deviceService.find("hardwareId*=" + searchVal).subscribe({
            next: (devices: QPageableReply<DeviceDto>) => {
              if (devices && devices.content.length > 0) {
                this.searchHardwareIds = devices.content.map(d => d.hardwareId);
              } else {
                this.searchHardwareIds = [];
              }
            }
          });
        } else {
          this.searchHardwareIds = [];
        }
      }
    });

    // Monitor for changes in search by measurement input, search by partial match.
    this.form.get("configuration_measurement")!.valueChanges.pipe(
      distinctUntilChanged()
    ).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.filteredUniqueMeasurements = this.allUniqueMeasurements!.filter(m => m.includes(searchVal));
        } else {
          this.filteredUniqueMeasurements = this.allUniqueMeasurements;
        }
      }
    });

    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());
  }

  save() {
    let di: DashboardItemDto = {
      id: this.incomingDi.id,
      type: this.incomingDi.type,
      title: this.form.get("title")!.value,
      subtitle: this.form.get("subtitle")!.value,
      columns: this.form.get("columns")!.value,
      index: this.incomingDi.index,
      enabled: this.incomingDi.enabled,
      configuration: JSON.stringify({
        hardwareId: this.form.get("configuration_hardwareId")!.value,
        measurement: this.form.get("configuration_measurement")!.value,
        unit: this.form.get("configuration_unit")!.value,
        icon: this.form.get("configuration_icon")!.value,
        precision: this.form.get("configuration_precision")!.value,
        sparkline: this.form.get("configuration_sparkline")!.value,
        sparklinePoints: this.form.get("configuration_sparkline_points")!.value,
        threshold: this.form.get("configuration_threshold")!.value,
        thresholdLow: this.form.get("configuration_threshold_low")!.value,
        thresholdLowColor: this.form.get("configuration_threshold_low_color")!.value,
        thresholdMiddle: this.form.get("configuration_threshold_middle")!.value,
        thresholdMiddleColor: this.form.get("configuration_threshold_middle_color")!.value,
        thresholdHigh: this.form.get("configuration_threshold_high")!.value,
        thresholdHighColor: this.form.get("configuration_threshold_high_color")!.value
      } as DashboardItemSensorConfigurationDto)
    }
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}
