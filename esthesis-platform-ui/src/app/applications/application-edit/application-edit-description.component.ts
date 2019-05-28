import {Component, Input, OnInit} from '@angular/core';
import {BaseComponent} from '../../shared/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {QFormsService} from '@eurodyn/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material';
import {UUID} from 'angular2-uuid';
import {ApplicationService} from '../application.service';
import * as _ from 'lodash';
import {KeyValueDto} from '../../dto/key-value-dto';
import {UtilityService} from '../../shared/utility.service';
import {OkCancelModalComponent} from '../../shared/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-application-edit-description',
  templateUrl: './application-edit-description.component.html',
  styleUrls: ['./application-edit-description.component.scss']
})
export class ApplicationEditDescriptionComponent  extends BaseComponent implements OnInit {
  @Input() id: number;

  form: FormGroup;

  constructor(private fb: FormBuilder, private applicationService: ApplicationService, private qForms: QFormsService,
              private route: ActivatedRoute, private router: Router, private dialog: MatDialog, private utilityService: UtilityService) {
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
