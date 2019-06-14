import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppSettings} from '../../app.settings';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';

@Component({
  selector: 'app-settings-networking',
  templateUrl: './settings-networking.component.html',
  styleUrls: ['./settings-networking.component.scss']
})
export class SettingsNetworkingComponent extends BaseComponent implements OnInit {
  form: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
              private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      mqttAclEndpointStatus: ['', [Validators.required]],
    });

    // Fetch settings.
    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.NETWORKING.MQTT_ACL_ENDPOINT_STATUS
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
