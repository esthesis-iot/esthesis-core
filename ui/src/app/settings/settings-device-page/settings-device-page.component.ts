import {Component, OnInit} from "@angular/core";
import {DevicePageFieldDto} from "../../dto/device-page-field-dto";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {DevicesService} from "../../devices/devices.service";
import {SettingsService} from "../settings.service";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-settings-device-page",
  templateUrl: "./settings-device-page.component.html",
  styleUrls: []
})
export class SettingsDevicePageComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;
  form!: FormGroup;
  settingsForm!: FormGroup;
  // allFields!: DevicePageFieldDto[];
  allUniqueMeasurements?: string[];
  fetchingGeoAttributes = true;

  constructor(private devicesService: DevicesService, private fb: FormBuilder,
    private utilityService: UtilityService, private qForms: QFormsService,
    private settingsService: SettingsService) {
    super();
  }

  ngOnInit() {
    // Setup forms.
    // Device page data fields.
    this.form = this.fb.group({
      fields: this.fb.array([])
    });

    // Other device settings.
    this.settingsForm = this.fb.group({});
    this.settingsForm.addControl(
      this.constants.DEVICE.SETTING.DEVICE_GEO_LAT, new FormControl(""));
    this.settingsForm.addControl(
      this.constants.DEVICE.SETTING.DEVICE_GEO_LON, new FormControl(""));

    // Fetch device page fields.
    this.settingsService.getDevicePageFields().subscribe(onNext => {
      // this.allFields = onNext;
      onNext
      .forEach(field => {
        // @ts-ignore
        this.form.controls.fields.push(this.createFieldElement(field));
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
    });
  }

  save() {
    // this.settingsService.save(
    //   _.map(Object.keys(this.settingsForm.controls), (fc) => {
    //     return new SettingDto(fc, this.settingsForm.get(fc)!.value);
    //   })).subscribe(onNext => {
    //   this.utilityService.popupSuccess("Settings saved successfully.");
    // });

    console.log(this.form);

    this.settingsService.saveDevicePageFields(
      this.qForms.cleanupData(this.form.getRawValue()).fields).subscribe(
      onNext => {
        this.utilityService.popupSuccess("Settings saved successfully.");
      });
  }

  newMeasurement() {
    // @ts-ignore
    this.form.controls.fields.push(this.createFieldElement({
      measurement: "",
      shown: true,
      label: "",
      formatter: "",
    }));
  }

  getFormFields() {
    // @ts-ignore
    return this.form.get("fields").controls;
  }

  deleteField(fieldIndex: number) {
    // @ts-ignore
    this.form.controls.fields.controls.splice(fieldIndex, 1);
  }

}
