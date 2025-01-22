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
import {
  DashboardItemDeviceMapConfigurationDto
} from "../../dto/configuration/dashboard-item-device-map-configuration-dto";
import {TagDto} from "../../../tags/dto/tag-dto";
import {TagsService} from "../../../tags/tags.service";

@Component({
  selector: 'app-dashboard-item-device-map-edit',
  templateUrl: './dashboard-item-device-map-edit.component.html'
})
export class DashboardItemDeviceMapEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  // A helper auto-complete container for devices matching the user's search input.
  searchHardwareIds?: string[];
  tags?: TagDto[];
  // Display a warning that no measurements are defined as coordinates in application settings.
  noCoordsWarning = false;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    private readonly fb: FormBuilder, private readonly deviceService: DevicesService,
    private readonly settingsService: SettingsService, private readonly utilityService: UtilityService,
    public dialogRef: MatDialogRef<DashboardItemDeviceMapEditComponent>,
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
      configuration_zoom_level: [0, [Validators.required]],
      configuration_map_lat: [[], []],
      configuration_map_lon: [[]],
      configuration_height: [null, [Validators.required]],
      search_hardwareId: [],
      search_tags: [],
    });
  }

  ngOnInit(): void {
    // Parse configuration.
    if (this.incomingDi.configuration != null) {
      const conf = JSON.parse(this.incomingDi.configuration) as DashboardItemDeviceMapConfigurationDto;
      this.form.patchValue({
        configuration_hardwareIds: conf.hardwareIds,
        configuration_tags: conf.tags,
        configuration_zoom_level: conf.zoom,
        configuration_map_lat: conf.mapLat,
        configuration_map_lon: conf.mapLon,
        configuration_height: conf.height
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
      }, error: (err) => {

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

    // Check if coordinates measurements are defined in application settings.
    this.settingsService.findByNames([AppConstants.DEVICE.SETTING.DEVICE_GEO_LAT, AppConstants.DEVICE.SETTING.DEVICE_GEO_LON]).subscribe({
      next: (res) => {
        if (res && res.length < 2) {
          this.noCoordsWarning = true;
        }
        console.log("res", res);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(
          "Could not fetch settings, please try again later.", error);
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
        zoom: this.form.get("configuration_zoom_level")!.value,
        height: this.form.get("configuration_height")!.value,
        mapLat: this.form.get("configuration_map_lat")!.value,
        mapLon: this.form.get("configuration_map_lon")!.value
      } as DashboardItemDeviceMapConfigurationDto)
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
}

