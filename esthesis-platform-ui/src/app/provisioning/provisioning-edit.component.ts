import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../shared/base-component';
import {MatDialog} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient, HttpEventType} from '@angular/common/http';
import {TagService} from '../tags/tag.service';
import {ProvisioningService} from './provisioning.service';
import {UtilityService} from '../shared/utility.service';
import {OkCancelModalComponent} from '../shared/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-provisioning-edit',
  templateUrl: './provisioning-edit.component.html',
  styleUrls: ['./provisioning-edit.component.scss']
})
export class ProvisioningEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  availableTags: string[];

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService, private tagService: TagService,
              private provisioningService: ProvisioningService, private route: ActivatedRoute,
              private router: Router, private http: HttpClient, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      description: ['', [Validators.maxLength(2048)]],
      file: ['', [Validators.required]],
      status: ['false', [Validators.required]],
      prerequisites: [''],
      defaultIP: [false],
      tags: [{value: [], disabled: false}],
      version: ['', [Validators.required]]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.provisioningService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // this.tagService.getTagNames().subscribe(onNext => {
    //   this.availableTags = onNext;
    // });
  }

  save() {
    this.provisioningService.saveBinary(this.form).subscribe(onEvent => {
      if (onEvent.type === HttpEventType.Response) {
        if (onEvent.status === 200) {
          this.utilityService.popupSuccess('Provisioning package successfully saved.');
          this.router.navigate(['provisioning']);
        } else {
          this.utilityService.popupError('There was a problem uploading the provisioning package.');
        }
      }
    }, onError => {
      this.utilityService.popupError('There was a problem uploading the provisioning package.');
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete Provisioning package',
        question: 'Do you really want to delete this Provisioning package?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.provisioningService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Provisioning package successfully deleted.');
          this.router.navigate(['provisioning']);
        });
      }
    });
  }

  selectFile(event) {
    this.form.controls['file'].patchValue(event.target.files[0]);
  }

}
