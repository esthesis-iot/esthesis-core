import {Component, OnInit} from "@angular/core";
import {FieldDto} from "../../dto/field-dto";
import {FormBuilder, FormGroup} from "@angular/forms";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {DevicesService} from "../../devices/devices.service";
import {SettingsService} from "../settings.service";
import {AppSettings} from "../../app.settings";
import * as _ from "lodash";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";
import {RegistryEntryDto} from "../../dto/registry-entry-dto";

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
  allFields!: FieldDto[];

  constructor(private devicesService: DevicesService, private fb: FormBuilder,
    private utilityService: UtilityService, private qForms: QFormsService,
    private settingsService: SettingsService) {
    super();
  }

  ngOnInit() {
    // Setup forms.
    this.form = this.fb.group({
      fields: this.fb.array([])
    });

    this.settingsForm = this.fb.group({
      geo_lon: ["", []],
      geo_lat: ["", []]
    });

    // Fetch fields.
    this.settingsService.getDevicePageFields().subscribe(onNext => {
      this.allFields = onNext;
      onNext
      .sort((a, b) => (a.measurement + a.field).localeCompare(b.measurement + b.field))
      .forEach(field => {
        // @ts-ignore
        this.form.controls["fields"].push(this.createFieldElement(field));
      });
    });

    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.GEOLOCATION.LATITUDE,
      AppSettings.SETTING.GEOLOCATION.LONGITUDE,
    ).subscribe(onNext => {
      onNext.forEach(registryEntryDto => {
        this.settingsForm.controls[registryEntryDto.name].patchValue(registryEntryDto.value);
      });
    });
  }

  createFieldElement(fieldDto: FieldDto) {
    return this.fb.group({
      id: [fieldDto?.id],
      measurement: [fieldDto?.measurement],
      field: [fieldDto?.field],
      shown: [fieldDto?.shown],
      label: [fieldDto?.label],
      datatype: [fieldDto?.datatype],
      valueHandler: [fieldDto?.valueHandler],
      formatter: [fieldDto?.formatter],
    });
  }

  save() {
    this.settingsService.saveDevicePageFields(
      this.qForms.cleanupData(this.form.getRawValue())["fields"]).subscribe(
      onNext => {
        this.settingsService.save(
          _.map(Object.keys(this.settingsForm.controls), (fc) => {
            return new RegistryEntryDto(fc, this.settingsForm.get(fc)!.value);
          })).subscribe(onNext => {
          this.utilityService.popupSuccess("Settings saved successfully.");
        });
      });
  }

  newMeasurement() {
    // @ts-ignore
    this.form.controls["fields"].push(this.createFieldElement({
      measurement: "",
      field: "",
      value: "",
      valueHandler: "",
      datatype: this.constants.MEASUREMENT_TYPE.TELEMETRY,
      label: "",
      formatter: "",
      shown: true,
    }));
  }

  getFormFields() {
    // @ts-ignore
    return this.form.get("fields")!["controls"];
  }

  deleteField(fieldIndex: number) {
    // @ts-ignore
    this.form.controls["fields"].controls.splice(fieldIndex, 1);
  }

}
