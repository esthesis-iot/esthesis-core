import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {QFormsService} from '@eurodyn/forms';
import {CaDto} from '../dto/ca-dto';
import {AppConstants} from '../app.constants';
import {CasService} from './cas.service';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';
import {OkCancelModalComponent} from '../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-cas-edit',
  templateUrl: './cas-edit.component.html',
  styleUrls: []
})
export class CasEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  parents: CaDto[];

  constructor(private fb: FormBuilder, private caService: CasService, private qForms: QFormsService,
              private route: ActivatedRoute, private router: Router, private dialog: MatDialog,
              private utilityService: UtilityService) {
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
      parentCa: [{value: '', disabled: this.id !== 0}],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.caService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // Fill dropdowns.
    this.caService.getEligibleForSigning().subscribe(onNext => {
      this.parents = onNext;
    });
  }

  save() {
    this.caService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess(
        this.form.value.id ? 'Certificate authority was successfully edited.'
          : 'Certificate authority was successfully created.');
      this.router.navigate(['cas']);
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete CA',
        question: 'Do you really want to delete this CA?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.caService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Root CA successfully deleted.');
          this.router.navigate(['cas']);
        });
      }
    });
  }

  downloadPublicKey(base64: boolean) {
    this.caService.download(this.id, AppConstants.KEY_TYPE.PUBLIC_KEY, base64);
  }

  downloadPrivateKey(base64: boolean) {
    this.caService.download(this.id, AppConstants.KEY_TYPE.PRIVATE_KEY, base64);
  }

  downloadCertificate(base64: boolean) {
    this.caService.download(this.id, AppConstants.KEY_TYPE.CERTIFICATE, base64);
  }

  downloadBackup() {
    this.caService.backup(this.id);
  }
}
