import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {TagDto} from '../../dto/tag-dto';
import {MatDialog} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {TagService} from '../../tags/tag.service';
import {ActivatedRoute, Router} from '@angular/router';
import {VirtualizationService} from './virtualization.service';
import {CertificatesService} from '../../certificates/certificates.service';
import {CertificateDto} from '../../dto/certificate-dto';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';
import {OkCancelModalComponent} from '../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-infrastructure-virtualization-edit',
  templateUrl: './infrastructure-virtualization-edit.component.html',
  styleUrls: ['./infrastructure-virtualization-edit.component.scss']
})
export class InfrastructureVirtualizationEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  availableTags: TagDto[];
  certificates: CertificateDto[];

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService, private tagService: TagService,
              private virtualizationService: VirtualizationService, private route: ActivatedRoute,
              private router: Router, private certificatesService: CertificatesService,
              private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      ipAddress: ['', [Validators.required, Validators.maxLength(255)]],
      serverType: ['', [Validators.required, Validators.maxLength(64)]],
      state: ['', [Validators.required, Validators.maxLength(16)]],
      security: ['', [Validators.required, Validators.maxLength(32)]],
      certificate: ['', [Validators.maxLength(12)]],
      tags: [[]],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.virtualizationService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
    this.tagService.getAll().subscribe(onNext => {
      this.availableTags = onNext.content;
    });
    this.certificatesService.getAll().subscribe(onNext => {
      this.certificates = onNext.content;
    });
  }

  save() {
    this.virtualizationService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Virtualization server successfully saved.');
      this.router.navigate(['infra'], {fragment: 'virtualization'});
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete Virtualization server',
        question: 'Do you really want to delete this Virtualization server?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.virtualizationService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Virtualization server successfully deleted.');
          this.router.navigate(['infra'], {fragment: 'virtualization'});
        });
      }
    });
  }

}
