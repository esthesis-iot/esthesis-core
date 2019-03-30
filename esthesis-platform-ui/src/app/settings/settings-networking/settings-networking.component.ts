import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppConstants} from '../../app.constants';
import {BaseComponent} from '../../shared/base-component';
import {SettingDto} from '../../dto/setting-dto';

@Component({
  selector: 'app-settings-networking',
  templateUrl: './settings-networking.component.html',
  styleUrls: ['./settings-networking.component.scss']
})
export class SettingsNetworkingComponent extends  BaseComponent implements OnInit {
  form: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      mqttAclEndpointStatus: ['', [Validators.required]],
    });

    // Fetch settings.
    this.settingsService.findByName(AppConstants.SETTING.MQTT.ACL_ENDPOINT_STATUS._KEY).subscribe(onNext => {
      this.form.controls[AppConstants.SETTING.MQTT.ACL_ENDPOINT_STATUS._KEY].patchValue(Number(onNext.val));
    });
  }

  save() {
    this.settingsService.save(new SettingDto(AppConstants.SETTING.MQTT.ACL_ENDPOINT_STATUS._KEY,
      this.form.controls[AppConstants.SETTING.MQTT.ACL_ENDPOINT_STATUS._KEY].value)).subscribe(() => {
    });
  }

}
