import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {QFormsService} from '@qlack/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient, HttpEventType} from '@angular/common/http';
import {TagService} from '../tags/tag.service';
import {ProvisioningService} from './provisioning.service';
import {TagDto} from '../dto/tag-dto';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';
import {
  OkCancelModalComponent
} from '../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-provisioning-edit',
  templateUrl: './provisioning-edit.component.html',
  styleUrls: []
})
export class ProvisioningEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id: number | undefined;
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService, private tagService: TagService,
              private provisioningService: ProvisioningService, private route: ActivatedRoute,
              private router: Router, private http: HttpClient, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      description: ['', [Validators.maxLength(2048)]],
      file: [{value: '', disabled: this.id !== 0}, [Validators.required]],
      state: [false, [Validators.required]],
      tags: [[]],
      packageVersion: ['', [Validators.required]],
      fileName: [''],
      encrypted: [false]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.provisioningService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });
  }

  save() {
    this.provisioningService.upload(this.form).subscribe(onEvent => {
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

  // // save() {
  //   this.provisioningService.save(this.form).subscribe(onEvent => {
  //     if (onEvent.type === HttpEventType.Response) {
  //       if (onEvent.status === 200) {
  //         this.utilityService.popupSuccess('Provisioning package successfully saved.');
  //         this.router.navigate(['provisioning']);
  //       } else {
  //         this.utilityService.popupError('There was a problem uploading the provisioning package.');
  //       }
  //     }
  //   }, onError => {
  //     this.utilityService.popupError('There was a problem uploading the provisioning package.');
  //   });
  // }

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

  selectFile(event: any) {
    this.form.controls['file'].patchValue(event.target.files[0]);
    this.form.controls['fileName'].patchValue(event.target.files[0].name);
  }

  download() {
    this.provisioningService.download(this.id!);
  }
}
