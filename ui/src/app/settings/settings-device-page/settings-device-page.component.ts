import {Component, OnInit} from "@angular/core";
import {DevicePageFieldDto} from "../../dto/device-page-field-dto";
import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {DevicesService} from "../../devices/devices.service";
import {SettingsService} from "../settings.service";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";
import {MatDialog} from "@angular/material/dialog";
import {
  MatIconPickerComponent
} from "../../shared/component/display/mat-icon-picker/mat-icon-picker.component";

@Component({
  selector: "app-settings-device-page",
  templateUrl: "./settings-device-page.component.html",
  styleUrls: ["./settings-device-page.component.scss"]
})
export class SettingsDevicePageComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;
  profileDataForm!: FormGroup;
  settingsForm!: FormGroup;
  allUniqueMeasurements?: string[];
  fetchingGeoAttributes = true;

  constructor(private devicesService: DevicesService, private fb: FormBuilder,
    private utilityService: UtilityService, private qForms: QFormsService,
    private settingsService: SettingsService, private dialog: MatDialog) {
    super();
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
    this.settingsService.getDevicePageFields().subscribe(fields => {
      // this.allFields = onNext;
      fields.forEach(field => {
        // @ts-ignore
        this.profileDataForm.controls.fields.push(this.createFieldElement(field));
      });
    });

    // Fetch Lat/Lon settings.
    this.settingsService.findByNames(
      this.constants.DEVICE.SETTING.DEVICE_GEO_LAT + ","
      + this.constants.DEVICE.SETTING.DEVICE_GEO_LON).subscribe(onNext => {
      onNext.forEach(setting => {
        this.settingsForm.controls[setting.name].patchValue(setting.value);
      });
    });

    // Fetch possible device measurements to be used in Lat/Lon settings.
    this.settingsService.findMeasurementNames().subscribe({
      next: next => {
        this.allUniqueMeasurements = next;
      }, error: err => {
        this.utilityService.popupErrorWithTraceId("Error fetching device measurements.", err);
      }, complete: () => {
        this.fetchingGeoAttributes = false;
      }
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
    this.settingsService.saveDevicePageFields(
      this.qForms.cleanupData(this.profileDataForm.getRawValue()).fields).subscribe(
      onNext => {
        this.utilityService.popupSuccess("Settings saved successfully.");
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
