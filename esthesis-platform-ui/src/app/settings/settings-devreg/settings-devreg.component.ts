import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../../shared/base-component';
import {SettingsService} from '../settings.service';
import {UtilityService} from '../../shared/utility.service';
import {AppSettings} from '../../app.settings';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';

@Component({
  selector: 'app-settings-devreg',
  templateUrl: './settings-devreg.component.html',
  styleUrls: ['./settings-devreg.component.scss']
})
export class SettingsDevregComponent extends BaseComponent implements OnInit {
  form: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
              private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      deviceRegistration: ['', [Validators.required]],
      devicePushTags: ['', [Validators.required]],
      ignoreDuringDeviceRegistration: ['', [Validators.required]],
    });


    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.DEVICE_REGISTRATION.REGISTRATION_MODE,
      AppSettings.SETTING.DEVICE_REGISTRATION.PUSH_TAGS,
      AppSettings.SETTING.DEVICE_REGISTRATION.IGNORE_DURING_DEVICE_REGISTRATION,
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
