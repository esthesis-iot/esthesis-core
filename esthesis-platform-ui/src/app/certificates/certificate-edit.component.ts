import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {QFormsService} from '@eurodyn/forms';
import {ActivatedRoute, Router} from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import {CertificatesService} from './certificates.service';
import {AppConstants} from '../app.constants';
import {CaDto} from '../dto/ca-dto';
import {CasService} from '../cas/cas.service';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';
import {OkCancelModalComponent} from '../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-certificate-edit',
  templateUrl: './certificate-edit.component.html',
  styleUrls: ['./certificate-edit.component.scss']
})
export class CertificateEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  issuers: CaDto[];

  constructor(private fb: FormBuilder, private certificatesService: CertificatesService,
              private qForms: QFormsService,
              private route: ActivatedRoute, private router: Router, private caService: CasService,
              private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      cn: [{value: '', disabled: this.id !== 0}, [Validators.maxLength(256)]],
      validity: [{value: '', disabled: this.id !== 0}, [Validators.required]],
      issuer: [{value: '', disabled: this.id !== 0}, []]
    });

    // Fill dropdowns.
    this.caService.getEligibleForSigning().subscribe(onNext => {
      this.issuers = onNext;
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.certificatesService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  save() {
    this.certificatesService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess(this.form.value.id ? 'Certificate was successfully edited.'
        : 'Certificate was successfully created.');
      this.router.navigate(['certificates']);
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete certificate',
        question: 'Do you really want to delete this certificate?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.certificatesService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Certificate successfully deleted.');
          this.router.navigate(['certificates']);
        });
      }
    });
  }

  downloadPublicKey(base64: boolean) {
    this.certificatesService.download(this.id, AppConstants.KEY_TYPE.PUBLIC_KEY, base64);
  }

  downloadPrivateKey(base64: boolean) {
    this.certificatesService.download(this.id, AppConstants.KEY_TYPE.PRIVATE_KEY, base64);
  }

  downloadCertificate(base64: boolean) {
    this.certificatesService.download(this.id, AppConstants.KEY_TYPE.CERTIFICATE, base64);
  }

  downloadBackup() {
    this.certificatesService.backup(this.id);
  }

}
