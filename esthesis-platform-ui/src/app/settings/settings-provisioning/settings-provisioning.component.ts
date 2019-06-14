import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppSettings} from '../../app.settings';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';

@Component({
  selector: 'app-settings-provisioning',
  templateUrl: './settings-provisioning.component.html',
  styleUrls: ['./settings-provisioning.component.scss']
})
export class SettingsProvisioningComponent extends BaseComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
              private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      provisioningUrl: ['',[]],
      provisioningEncrypt: ['',[]],
      provisioningSign: ['',[]],
    });


    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.PROVISIONING.PROVISIONING_URL,
      AppSettings.SETTING.PROVISIONING.ENCRYPTION,
      AppSettings.SETTING.PROVISIONING.SIGNATURE
    ).subscribe(onNext => {
      onNext.forEach(settingDTO => {
        this.form.controls[settingDTO.key].patchValue(settingDTO.val);
      })
    });
  }

  save() {
    this.settingsService.saveMultiple(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new KeyValueDto(fc, this.form.get(fc).value)
      })).subscribe(onNext => {
      this.utilityService.popupSuccess("Settings saved successfully.");
    });
  }

}
