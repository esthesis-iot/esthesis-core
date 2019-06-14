import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppSettings} from '../../app.settings';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';

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
      devicePushTags: ['', [Validators.required]]
    });


    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.DEVICE_REGISTRATION.REGISTRATION_MODE,
      AppSettings.SETTING.DEVICE_REGISTRATION.PUSH_TAGS,
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
