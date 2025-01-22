import {Component, OnInit} from "@angular/core";
import {DevicePageFieldDefinitionDto} from "../dto/device-page-field-definition-dto";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {AppConstants} from "../../app.constants";
import {SettingDto} from "../dto/setting-dto";
import * as _ from "lodash-es";
import {forkJoin} from "rxjs";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-settings-device-page",
  templateUrl: "./settings-device-page.component.html"
})
export class SettingsDevicePageComponent extends SecurityBaseComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;
  profileDataForm!: FormGroup;
  settingsForm!: FormGroup;
  allUniqueMeasurements?: string[];
  fetchingGeoAttributes = true;

  constructor(private readonly fb: FormBuilder, private readonly utilityService: UtilityService,
    private readonly settingsService: SettingsService) {
    super(AppConstants.SECURITY.CATEGORY.SETTINGS);
  }

  ngOnInit() {
    // Setup forms.
    // Device page data fields.
    this.profileDataForm = this.fb.group({
      fields: this.fb.array([])
    });

    // Other device settings.
    this.settingsForm = this.fb.group({});
    this.settingsForm.addControl(
      this.constants.DEVICE.SETTING.DEVICE_GEO_LAT, new FormControl(""));
    this.settingsForm.addControl(
      this.constants.DEVICE.SETTING.DEVICE_GEO_LON, new FormControl(""));

    // Fetch device page fields.
    this.settingsService.getDevicePageFields().subscribe({
      next: (fields: DevicePageFieldDefinitionDto[]) => {
        fields.forEach(field => {
          // @ts-ignore
          this.profileDataForm.controls.fields.push(this.createFieldElement(field));
        });
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Error fetching device page fields.", err);
      }
    });

    // Fetch Lat/Lon settings.
    this.settingsService.findByNames([
      this.constants.DEVICE.SETTING.DEVICE_GEO_LAT,
      this.constants.DEVICE.SETTING.DEVICE_GEO_LON
    ]).subscribe({
      next: (settings: SettingDto[]) => {
        settings.forEach(setting => {
          this.settingsForm.controls[setting.name].patchValue(setting.value);
        });
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Error fetching settings for geolocation fields.", err);
      }
    });

    // Fetch possible device measurements to be used in Lat/Lon settings.
    this.settingsService.findMeasurementNames().subscribe({
      next: next => {
        this.allUniqueMeasurements = next;
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Error fetching device measurements.", err);
      }
    }).add(() => {
      this.fetchingGeoAttributes = false;
    });
  }

  createFieldElement(fieldDto: DevicePageFieldDefinitionDto) {
    return this.fb.group({
      id: [fieldDto.id],
      measurement: [fieldDto?.measurement],
      shown: [fieldDto?.shown],
      label: [fieldDto?.label],
      formatter: [fieldDto?.formatter]
    });
  }

  save() {
    forkJoin([
      this.settingsService.save(
        _.map(Object.keys(this.settingsForm.controls), (fc) => {
          return new SettingDto(fc, this.settingsForm.get(fc)!.value);
        })),
      this.settingsService.saveDevicePageFields(this.profileDataForm.getRawValue().fields)
    ]).subscribe({
      next: () => {
        this.utilityService.popupSuccess("Settings saved successfully.");
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Error saving settings.", err);
      }
    });
  }

  newMeasurement() {
    // @ts-ignore
    this.profileDataForm.controls.fields.push(this.createFieldElement({
      measurement: "",
      shown: true,
      label: "",
      formatter: ""
    }));
  }

  getFormFields() {
    // @ts-ignore
    return this.profileDataForm.get("fields").controls;
  }

  deleteField(fieldIndex: number) {
    // @ts-ignore
    this.profileDataForm.controls.fields.controls.splice(fieldIndex, 1);
  }
}
