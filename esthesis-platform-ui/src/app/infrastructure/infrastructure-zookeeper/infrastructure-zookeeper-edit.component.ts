import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {QFormsService} from '@eurodyn/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {CertificateDto} from '../../dto/certificate-dto';
import {CertificatesService} from '../../certificates/certificates.service';
import {ZookeeperServerService} from './zookeeper-server.service';
import {CaDto} from '../../dto/ca-dto';
import {CasService} from '../../cas/cas.service';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from 'src/app/shared/service/utility.service';
import {OkCancelModalComponent} from '../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-infrastructure-zookeeper-edit',
  templateUrl: './infrastructure-zookeeper-edit.component.html',
  styleUrls: ['./infrastructure-zookeeper-edit.component.scss']
})
export class InfrastructureZookeeperEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  certificates: CertificateDto[];
  cas: CaDto[];

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService,
              private zookeeperServerService: ZookeeperServerService, private route: ActivatedRoute,
              private router: Router, private utilityService: UtilityService,
              private certificatesService: CertificatesService, private casService: CasService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      ipAddress: ['', [Validators.required, Validators.maxLength(256)]],
      state: ['', [Validators.required, Validators.maxLength(5)]],
      username: ['', []],
      password: ['', []],
      caCert: ['', []],
      clientCert: ['', []],
      clientKey: ['', []],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.zookeeperServerService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // Get certificates.
    this.certificatesService.getAll().subscribe(onNext => {
      this.certificates = onNext.content;
    });

    // Get CAs.
    this.casService.getAll().subscribe(onNext => {
      this.cas = onNext.content;
    });
  }

  save() {
    this.zookeeperServerService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Zookeeper server successfully saved.');
      this.router.navigate(['infra'], {fragment: 'zookeeper'});
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete Zookeeper server',
        question: 'Do you really want to delete this Zookeeper server?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.zookeeperServerService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Zookeeper server successfully deleted.');
          this.router.navigate(['infra'], {fragment: 'zookeeper'});
        });
      }
    });
  }

  pickCertificate(certificateId: number, control: string) {
    this.certificatesService.get(certificateId).subscribe(onNext => {
      this.form.get(control).setValue(onNext.certificate);
    });
  }

  pickCaCertificate(caId: number, control: string) {
    this.certificatesService.get(caId).subscribe(onNext => {
      this.form.get(control).setValue(onNext.certificate);
    });
  }

  pickPrivateKey(certificateId: number, control: string) {
    this.certificatesService.get(certificateId).subscribe(onNext => {
      this.form.get(control).setValue(onNext.privateKey);
    });
  }

}
