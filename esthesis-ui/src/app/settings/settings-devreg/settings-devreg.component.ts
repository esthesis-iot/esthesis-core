import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppSettings} from '../../app.settings';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';
import {CasService} from '../../cas/cas.service';
import {CaDto} from '../../dto/ca-dto';

@Component({
  selector: 'app-settings-devreg',
  templateUrl: './settings-devreg.component.html',
  styleUrls: []
})
export class SettingsDevregComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  cas: CaDto[];

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
              private utilityService: UtilityService, private casService: CasService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      deviceRegistration: ['', [Validators.required]],
      deviceTagsAlgorithm: ['', [Validators.required]],
      deviceRootCA: ['', []]
    });


    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.DEVICE_REGISTRATION.REGISTRATION_MODE,
      AppSettings.SETTING.DEVICE_REGISTRATION.TAGS_ALGORITHM,
      AppSettings.SETTING.DEVICE_REGISTRATION.ROOT_CA,
    ).subscribe(onNext => {
      onNext.forEach(settingDTO => {
        this.form.controls[settingDTO.key].patchValue(settingDTO.val);
      });
    });

    // Fetch lookup values.
    this.casService.getAll('sort=cn,asc').subscribe(onNext => {
      if (onNext.content && onNext.content.length > 0) {
        onNext.content.unshift(new CaDto(null, ''));
        this.cas = onNext.content;
      }
    });
  }

  save() {
    this.settingsService.saveMultiple(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new KeyValueDto(fc, this.form.get(fc).value);
      })).subscribe(onNext => {
      this.utilityService.popupSuccess('Settings saved successfully.');
    });
  }

}
