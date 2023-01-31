import {Component, OnInit} from "@angular/core";
import {DevicePageFieldDto} from "../../devices/dto/device-page-field-dto";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {UtilityService} from "../../shared/service/utility.service";
import {DevicesService} from "../../devices/devices.service";
import {SettingsService} from "../settings.service";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";
import {MatDialog} from "@angular/material/dialog";
import {
  MatIconPickerComponent
} from "../../shared/component/display/mat-icon-picker/mat-icon-picker.component";
import {SettingDto} from "../dto/setting-dto";
import * as _ from "lodash-es";
import {forkJoin} from "rxjs";

@Component({
  selector: "app-settings-device-page",
  templateUrl: "./settings-device-page.component.html",
  styleUrls: ["./settings-device-page.component.scss"]
})
export class SettingsDevicePageComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;
  profileDataForm!: FormGroup;
  settingsForm!: FormGroup;
  allUniqueMeasurements?: string[];
  fetchingGeoAttributes = true;

  constructor(private devicesService: DevicesService, private fb: FormBuilder,
    private utilityService: UtilityService, private qForms: QFormsService,
    private settingsService: SettingsService, private dialog: MatDialog) {
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
      next: (fields: DevicePageFieldDto[]) => {
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

  createFieldElement(fieldDto: DevicePageFieldDto) {
    return this.fb.group({
      id: [fieldDto.id],
      measurement: [fieldDto?.measurement],
      shown: [fieldDto?.shown],
      label: [fieldDto?.label],
      formatter: [fieldDto?.formatter],
      icon: [fieldDto?.icon],
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
      formatter: "",
      icon: ""
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

  selectIcon(i: number) {
    this.dialog.open(MatIconPickerComponent).afterClosed().subscribe(result => {
      const field = (this.profileDataForm.controls.fields as FormArray).controls[i].value as DevicePageFieldDto;
      field.icon = result;
      (this.profileDataForm.controls.fields as FormArray).controls[i].patchValue(field);
    });
  }
}
