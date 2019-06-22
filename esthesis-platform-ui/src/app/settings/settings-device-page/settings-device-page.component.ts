import {Component, OnInit} from '@angular/core';
import {FieldDto} from '../../dto/field-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';
import {QFormsService} from '@eurodyn/forms';
import {DevicesService} from '../../devices/devices.service';
import {SettingsService} from '../settings.service';
import {AppSettings} from '../../app.settings';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';

@Component({
  selector: 'app-settings-device-page',
  templateUrl: './settings-device-page.component.html',
  styleUrls: ['./settings-device-page.component.scss']
})
export class SettingsDevicePageComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  settingsForm: FormGroup;
  allFields: FieldDto[];

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
      geo_lon: ['',[]],
      geo_lat: ['',[]]
    });

    // Fetch fields.
    this.devicesService.getFields().subscribe(onNext => {
      this.allFields = onNext;
      onNext.forEach(field => {
        // @ts-ignore
        this.form.controls['fields'].push(this.createFieldElement(field));
      })
    });

    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.GEOLOCATION.LATITUDE,
      AppSettings.SETTING.GEOLOCATION.LONGITUDE,
    ).subscribe(onNext => {
      onNext.forEach(settingDTO => {
        this.settingsForm.controls[settingDTO.key].patchValue(settingDTO.val);
      })
    });
  }

  createFieldElement(fieldDto: FieldDto) {
    return this.fb.group({
      name: [{value: fieldDto.name, disabled: true}, Validators.required],
      datatype: [{value: fieldDto.datatype, disabled: true}, Validators.required],
      shown: [fieldDto.shown],
      label: [fieldDto.label],
      valueHandler: [fieldDto.valueHandler],
      formatter: [fieldDto.formatter],
    });
  }

  save() {
    this.devicesService.saveFields(this.qForms.cleanupForm(this.form)['fields']).subscribe(
      onNext => {
        this.settingsService.saveMultiple(
          _.map(Object.keys(this.settingsForm.controls), (fc) => {
            return new KeyValueDto(fc, this.settingsForm.get(fc).value)
          })).subscribe(onNext => {
          this.utilityService.popupSuccess("Settings saved successfully.");
        });
      });
  }
}
