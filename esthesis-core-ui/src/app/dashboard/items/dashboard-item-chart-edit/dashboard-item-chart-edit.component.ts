import {Component, Inject, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {DevicesService} from "../../../devices/devices.service";
import {SettingsService} from "../../../settings/settings.service";
import {UtilityService} from "../../../shared/services/utility.service";
import {AppConstants} from "../../../app.constants";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {DeviceDto} from "../../../devices/dto/device-dto";
import {TagDto} from "../../../tags/dto/tag-dto";
import {TagsService} from "../../../tags/tags.service";
import {
  DashboardItemChartConfigurationDto
} from "../../dto/configuration/dashboard-item-chart-configuration-dto";

@Component({
  selector: 'app-dashboard-item-chart-edit',
  templateUrl: './dashboard-item-chart-edit.component.html'
})
export class DashboardItemChartEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  // Auto-complete complete helpers.
  searchHardwareIds?: string[];
  allUniqueMeasurements?: string[];
  filteredUniqueMeasurements?: string[];
  tags?: TagDto[];

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    private readonly fb: FormBuilder, private readonly deviceService: DevicesService,
    private readonly settingsService: SettingsService, private readonly utilityService: UtilityService,
    public dialogRef: MatDialogRef<DashboardItemChartEditComponent>,
    private readonly tagService: TagsService) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD);

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_hardwareIds: [[]],
      configuration_tags: [[]],
      configuration_measurements: [[]],
      configuration_height: [null, [Validators.required]],
      configuration_totalPoints: [null, [Validators.required]],
      configuration_lineTension: [null, [Validators.required]],
      search_hardwareId: [],
      search_measurement: []
    });
  }

  ngOnInit(): void {
    // Parse configuration.
    if (this.incomingDi.configuration != null) {
      const conf = JSON.parse(this.incomingDi.configuration) as DashboardItemChartConfigurationDto;
      this.form.patchValue({
        configuration_hardwareIds: conf.hardwareIds,
        configuration_tags: conf.tags,
        configuration_measurements: conf.measurements,
        configuration_height: conf.height,
        configuration_totalPoints: conf.totalPoints,
        configuration_lineTension: conf.lineTension
      })
    }

    // Monitor for changes in search by hardware id input.
    this.form.get("search_hardwareId")!.valueChanges.pipe(
      debounceTime(300), distinctUntilChanged()
    ).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.deviceService.findDeviceByPartialHardwareId(searchVal).subscribe({
            next: (devices: DeviceDto[]) => {
              if (devices && devices.length > 0) {
                this.searchHardwareIds = devices.map(d => d.hardwareId);
              } else {
                this.searchHardwareIds = [];
              }
            }, error: (error) => {
              this.utilityService.popupErrorWithTraceId(
                "Could not fetch devices, please try again later.", error);
              this.searchHardwareIds = [];
            }
          });
        } else {
          this.searchHardwareIds = [];
        }
      }
    });

    // Monitor for changes in search by measurement input, search by partial match.
    this.form.get("search_measurement")!.valueChanges.pipe(
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

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe({
      next: (next) => {
        this.tags = next.content;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "Could not fetch tags, please try again later.", error);
      }
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
        hardwareIds: this.form.get("configuration_hardwareIds")!.value,
        tags: this.form.get("configuration_tags")!.value,
        measurements: this.form.get("configuration_measurements")!.value,
        height: this.form.get("configuration_height")!.value,
        totalPoints: this.form.get("configuration_totalPoints")!.value,
        lineTension: this.form.get("configuration_lineTension")!.value
      } as DashboardItemChartConfigurationDto)
    }
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }

  addDevice() {
    // Before adding the device, check if it's already in the list.
    if (!this.form.get("configuration_hardwareIds")?.value.includes(this.form.get("search_hardwareId")?.value)) {
      this.form.get("configuration_hardwareIds")?.value.push(this.form.get("search_hardwareId")?.value);
    }
    this.form.get("search_hardwareId")?.setValue(null);
  }

  removeHardwareId(hardwareId: string) {
    this.form.get("configuration_hardwareIds")?.value
      .splice(this.form.get("configuration_hardwareIds")?.value.indexOf(hardwareId), 1);
  }

  addMeasurement() {
    // Before adding a measurement, check if it's already in the list.
    let measurement = this.form.get("search_measurement")?.value;
    if (!this.form.get("configuration_measurements")?.value.includes(measurement)) {
      this.form.get("configuration_measurements")?.value.push(measurement);
    }
    this.form.get("search_measurement")?.setValue(null);
  }

  removeMeasurement(measurement: string) {
    this.form.get("configuration_measurements")?.value
    .splice(this.form.get("configuration_measurements")?.value.indexOf(measurement), 1);
  }
}

