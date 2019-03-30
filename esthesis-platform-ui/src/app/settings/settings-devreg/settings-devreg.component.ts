import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AppConstants} from '../../app.constants';
import {BaseComponent} from '../../shared/base-component';
import {SettingDto} from '../../dto/setting-dto';
import {SettingsService} from '../settings.service';
import {UtilityService} from '../../shared/utility.service';

@Component({
  selector: 'app-settings-devreg',
  templateUrl: './settings-devreg.component.html',
  styleUrls: ['./settings-devreg.component.scss']
})
export class SettingsDevregComponent extends BaseComponent implements OnInit {
  form: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService, private utilityService: UtilityService,) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      deviceRegistration: ['', [Validators.required]]
    });

    // Fetch settings.
    this.settingsService.findByName(AppConstants.SETTING.DEVICE_REGISTRATION._KEY).subscribe(onNext => {
      this.form.controls[AppConstants.SETTING.DEVICE_REGISTRATION._KEY].setValue(onNext.val);
    });
  }

  save() {
    this.settingsService.save(new SettingDto(AppConstants.SETTING.DEVICE_REGISTRATION._KEY,
      this.form.controls[AppConstants.SETTING.DEVICE_REGISTRATION._KEY].value)).subscribe(() => {
      this.utilityService.popupSuccess('Device registration mode successfully saved.');
    });
  }

}
