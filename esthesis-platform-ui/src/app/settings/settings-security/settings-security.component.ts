import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../shared/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {AppConstants} from '../../app.constants';
import {SettingDto} from '../../dto/setting-dto';
import {CertificatesService} from '../../certificates/certificates.service';
import {CertificateDto} from '../../dto/certificate-dto';

@Component({
  selector: 'app-settings-security',
  templateUrl: './settings-security.component.html',
  styleUrls: ['./settings-security.component.scss']
})
export class SettingsSecurityComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  certificates: CertificateDto[];

  constructor(private fb: FormBuilder, private settingsService: SettingsService, private certificatesService: CertificatesService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      deviceDataEncryptionMode: ['', [Validators.required]],
      mqttSuperuserCertificate: ['', []],
    });

    // Fetch settings.
    this.settingsService.findByName(AppConstants.SETTING.DEVICE_DATA_ENCRYPTION_MODE._KEY).subscribe(onNext => {
      this.form.controls[AppConstants.SETTING.DEVICE_DATA_ENCRYPTION_MODE._KEY].patchValue(Number(onNext.val));
    });
    this.settingsService.findByName(AppConstants.SETTING.MQTT.SUPERUSER_CERTIFICATE._KEY).subscribe(onNext => {
      this.form.controls[AppConstants.SETTING.MQTT.SUPERUSER_CERTIFICATE._KEY].patchValue(Number(onNext.val));
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
    this.settingsService.save(new SettingDto(AppConstants.SETTING.DEVICE_DATA_ENCRYPTION_MODE._KEY,
      this.form.controls[AppConstants.SETTING.DEVICE_DATA_ENCRYPTION_MODE._KEY].value)).subscribe(() => {
    });
    this.settingsService.save(new SettingDto(AppConstants.SETTING.MQTT.SUPERUSER_CERTIFICATE._KEY,
      this.form.controls[AppConstants.SETTING.MQTT.SUPERUSER_CERTIFICATE._KEY].value)).subscribe(() => {
    });
  }
}
