import {Component, Inject, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/view-edit/dashboard-item-dto";
import {DevicesService} from "../../../devices/devices.service";
import {SettingsService} from "../../../settings/settings.service";
import {UtilityService} from "../../../shared/services/utility.service";
import {AppConstants} from "../../../app.constants";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {DeviceDto} from "../../../devices/dto/device-dto";
import {MatTableDataSource} from "@angular/material/table";
import {
  DashboardItemSensorIconConditionConfigurationDto
} from "../../dto/configuration/dashboard-item-sensor-icon-condition-configuration-dto";
import {
  DashboardItemSensorIconConfigurationDto
} from "../../dto/configuration/dashboard-item-sensor-icon-configuration-dto";

@Component({
  selector: 'app-dashboard-item-sensor-icon-edit',
  templateUrl: './dashboard-item-sensor-icon-edit.component.html'
})
export class DashboardItemSensorIconEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  // A helper auto-complete container for devices matching the user's search input.
  searchHardwareIds?: string[];
  // A helper auto-complete container for measurement names.
  allUniqueMeasurements?: string[];
  filteredUniqueMeasurements?: string[];
  displayedColumns = ["condition", "icon", "action"];
  dataSource = new MatTableDataSource<DashboardItemSensorIconConditionConfigurationDto>();

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    private readonly fb: FormBuilder, private readonly deviceService: DevicesService,
    private readonly settingsService: SettingsService, private readonly utilityService: UtilityService,
    public dialogRef: MatDialogRef<DashboardItemSensorIconEditComponent>) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD);
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemSensorIconConfigurationDto;
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
      configuration_precision: [configuration?.precision, [Validators.pattern('^[0-9]+$')]],
      configuration_conditions: this.fb.array([])
    });

    // Fill-in conditions.
    if (configuration?.conditions) {
      configuration.conditions.forEach((condition: DashboardItemSensorIconConditionConfigurationDto) => {
        // @ts-ignore
        this.form.controls.configuration_conditions.push(this.createCondition(condition));
      });
    }

    // Fetch possible device measurements to be used in Lat/Lon settings.
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
          this.deviceService.findDeviceByPartialHardwareId(searchVal).subscribe({
            next: (devices: DeviceDto[]) => {
              if (devices && devices.length > 0) {
                this.searchHardwareIds = devices.map(d => d.hardwareId);
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
        precision: this.form.get("configuration_precision")!.value,
        conditions: this.form.get("configuration_conditions")!.value
      } as DashboardItemSensorIconConfigurationDto)
    }
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }

  getConditions() {
    // @ts-ignore
    return this.form.get("configuration_conditions").controls;
  }

  private createCondition(condition: DashboardItemSensorIconConditionConfigurationDto) {
    return this.fb.group({
      condition: [condition.condition, [Validators.required]],
      icon: [condition.icon, [Validators.required]],
    });
  }

  addCondition() {
    const condition = new DashboardItemSensorIconConditionConfigurationDto("", "");
    // @ts-ignore
    this.form.controls.configuration_conditions.push(this.createCondition(condition));
  }

  removeCondition(i: number) {
    (this.form.controls['configuration_conditions'] as FormArray).removeAt(i);
  }
}

