import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../shared/base-component';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppConstants} from '../../app.constants';
import {SettingDto} from '../../dto/setting-dto';
import {CertificatesService} from '../../certificates/certificates.service';
import {CertificateDto} from '../../dto/certificate-dto';
import {KeyValueDto} from '../../dto/key-value-dto';
import * as _ from 'lodash';
import {UtilityService} from '../../shared/utility.service';

@Component({
  selector: 'app-settings-security',
  templateUrl: './settings-security.component.html',
  styleUrls: ['./settings-security.component.scss']
})
export class SettingsSecurityComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  certificates: CertificateDto[];

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
              private certificatesService: CertificatesService, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      deviceOutgoingEncryption: ['', [Validators.required]],
      deviceIncomingEncryption: ['', [Validators.required]],
      deviceOutgoingSignature: ['', [Validators.required]],
      deviceIncomingSignature: ['', [Validators.required]],
      mqttSuperuserCertificate: ['', []],
    });

    // Fetch settings.
    this.settingsService.findByNames(
      AppConstants.SETTING.DEVICE_OUTGOING_ENCRYPTION._KEY,
      AppConstants.SETTING.DEVICE_INCOMING_ENCRYPTION._KEY,
      AppConstants.SETTING.DEVICE_OUTGOING_SIGNATURE._KEY,
      AppConstants.SETTING.DEVICE_INCOMING_SIGNATURE._KEY,
      AppConstants.SETTING.MQTT.SUPERUSER_CERTIFICATE._KEY
    ).subscribe(onNext => {
      onNext.forEach(settingDTO => {
        this.form.controls[settingDTO.key].patchValue(settingDTO.val);
      })
    });

    // Fetch lookup values.
    this.certificatesService.getAll('sort=cn,asc').subscribe(onNext => {
      if (onNext.content.length > 0) {
        onNext.content.unshift(new CertificateDto(null, ''));
        this.certificates = onNext.content;
      }
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
