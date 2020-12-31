import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import {UUID} from 'angular2-uuid';
import {ApplicationService} from '../application.service';
import {BaseComponent} from '../../shared/component/base-component';
import {OkCancelModalComponent} from 'src/app/shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {UtilityService} from '../../shared/service/utility.service';
import {QFormsService} from '@qlack/forms';

@Component({
  selector: 'app-application-edit-description',
  templateUrl: './application-edit-description.component.html',
  styleUrls: []
})
export class ApplicationEditDescriptionComponent extends BaseComponent implements OnInit {
  @Input() id: number | undefined;
  form!: FormGroup;

  constructor(private fb: FormBuilder, private applicationService: ApplicationService,
              private qForms: QFormsService,
              private route: ActivatedRoute, private router: Router, private dialog: MatDialog,
              private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.maxLength(256)]],
      token: ['', [Validators.required, Validators.maxLength(256)]],
      state: ['', [Validators.required]]
    });

    if (this.id && this.id !== 0) {
      // Fill-in the form with data if editing an existing item.
      this.applicationService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  generateToken() {
    this.form.controls['token'].setValue(UUID.UUID());
  }

  save() {
    this.applicationService.save(this.form.getRawValue()).subscribe(onNext => {
      this.utilityService.popupSuccess(this.form.value.id ? 'Application was successfully edited.'
        : 'Application was successfully created.');
      this.router.navigate(['applications']);
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete Application',
        question: 'Do you really want to delete this Application?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.applicationService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Application successfully deleted.');
          this.router.navigate(['applications']);
        });
      }
    });
  }
}
