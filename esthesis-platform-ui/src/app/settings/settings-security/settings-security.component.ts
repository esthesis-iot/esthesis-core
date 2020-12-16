import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SettingsService} from '../settings.service';
import {CertificatesService} from '../../certificates/certificates.service';
import {CertificateDto} from '../../dto/certificate-dto';
import {KeyValueDto} from '../../dto/key-value-dto';
import * as _ from 'lodash';
import {AppSettings} from '../../app.settings';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';

@Component({
  selector: 'app-settings-security',
  templateUrl: './settings-security.component.html',
  styleUrls: []
})
export class SettingsSecurityComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  certificates: CertificateDto[] | undefined;

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
      platformCertificate: ['', [Validators.required]],
    });

    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.SECURITY.OUTGOING_ENCRYPTION,
      AppSettings.SETTING.SECURITY.INCOMING_ENCRYPTION,
      AppSettings.SETTING.SECURITY.OUTGOING_SIGNATURE,
      AppSettings.SETTING.SECURITY.INCOMING_SIGNATURE,
      AppSettings.SETTING.SECURITY.PLATFORM_CERTIFICATE,
    ).subscribe(onNext => {
      onNext.forEach(settingDTO => {
        this.form.controls[settingDTO.key].patchValue(settingDTO.val);
      })
    });

    // Fetch lookup values.
    this.certificatesService.getAll('sort=cn,asc').subscribe(onNext => {
      if (onNext.content && onNext.content.length > 0) {
        onNext.content.unshift(new CertificateDto(null!, ''));
        this.certificates = onNext.content;
      }
    });
  }

  save() {
    this.settingsService.saveMultiple(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new KeyValueDto(fc, this.form.get(fc)!.value)
      })).subscribe(onNext => {
      this.utilityService.popupSuccess("Settings saved successfully.");
    });
  }
}
