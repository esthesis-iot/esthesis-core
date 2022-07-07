import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {UUID} from 'angular2-uuid';
import {TagService} from './tag.service';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';
import {
  OkCancelModalComponent
} from '../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {TagDto} from '../dto/tag-dto';
import {QFormsService} from '@qlack/forms';
import {QFormValidationEEService} from "../shared/service/form-validation.service";

@Component({
  selector: 'app-tag-edit',
  templateUrl: './tag-edit.component.html',
  styleUrls: []
})
export class TagEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;

  constructor(private fb: FormBuilder, private tagService: TagService,
              private route: ActivatedRoute,
              private qForms: QFormsService, private router: Router,
              private utilityService: UtilityService,
              private dialog: MatDialog,
              // private qFormValidation: QFormValidationService) {
              private qFormValidation: QFormValidationEEService) {
    super();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id')!;

    // Setup the form.
    this.form = this.fb.group({
      id: [{value: '', disabled: true}],
      name: [{
        value: '',
        disabled: false
      }, [Validators.maxLength(1024)]],
    });
    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.tagService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    } else {
      this.form.patchValue({
        salt: UUID.UUID()
      });
    }
  }

  save() {
    this.tagService.save(this.qForms.cleanupData(this.form.getRawValue()) as TagDto).subscribe(
      onSuccess => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess('Tag was successfully created.');
        } else {
          this.utilityService.popupSuccess('Tag was successfully edited.');
        }
        this.router.navigate(['tags']);
      }, onError => {
        if (onError.status == 400) {
          console.log("ERROR!");
          let validationErrors = onError.error;
          if (validationErrors) {
            // @ts-ignore
            this.qFormValidation.validateForm(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError('There was an error trying to save this tag.');
        }
      });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete Tag',
        question: 'Do you really want to delete this Tag?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.tagService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Tag successfully deleted.');
          this.router.navigate(['tags']);
        });
      }
    });
  }
}
