import { Component, OnInit } from '@angular/core';
import {BaseComponent} from '../../shared/component/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TagDto} from '../../dto/tag-dto';
import {CertificateDto} from '../../dto/certificate-dto';
import {CaDto} from '../../dto/ca-dto';
import {MatDialog} from '@angular/material/dialog';
import {QFormsService} from '@eurodyn/forms';
import {TagService} from '../../tags/tag.service';
import {MqttServerService} from '../infrastructure-mqtt/mqtt-server.service';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../../shared/service/utility.service';
import {CertificatesService} from '../../certificates/certificates.service';
import {CasService} from '../../cas/cas.service';
import {OkCancelModalComponent} from '../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {NiFiDto} from '../../dto/ni-fi-dto';
import {NiFiService} from './nifi.service';

@Component({
  selector: 'app-infrastructure-nifi-edit',
  templateUrl: './infrastructure-nifi-edit.component.html',
  styleUrls: ['./infrastructure-nifi-edit.component.scss']
})
export class InfrastructureNifiEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  nifi: NiFiDto[];

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService,
              private nifiService: NiFiService, private route: ActivatedRoute,
              private router: Router, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      url: ['', [Validators.required, Validators.maxLength(2048)]],
      description: ['', [Validators.maxLength(4096)]],
      state: ['', [Validators.required, Validators.maxLength(5)]]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.nifiService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  save() {
    this.nifiService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('NiFi server successfully saved.');
      this.router.navigate(['infra'], {fragment: 'nifi'});
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete NiFi server',
        question: 'Do you really want to delete this NiFi server?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.nifiService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('NiFi server successfully deleted.');
          this.router.navigate(['infra'], {fragment: 'nifi'});
        });
      }
    });
  }

}
