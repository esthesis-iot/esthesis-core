import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {QFormsService} from '@eurodyn/forms';
import {MatDialog} from '@angular/material';
import {UUID} from 'angular2-uuid';
import {BaseComponent} from '../shared/base-component';
import {TagService} from './tag.service';
import {UtilityService} from '../shared/utility.service';
import 'rxjs/add/operator/debounceTime';
import {OkCancelModalComponent} from '../shared/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-tag-edit',
  templateUrl: './tag-edit.component.html',
  styleUrls: ['./tag-edit.component.scss']
})
export class TagEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;

  constructor(private fb: FormBuilder, private tagService: TagService, private route: ActivatedRoute,
              private qForms: QFormsService, private router: Router, private utilityService: UtilityService,
              private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [{value: '', disabled: true}],
      name: [{
        value: '',
        disabled: false
      }, [Validators.maxLength(1024)]],
    });
    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.tagService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    } else {
      this.form.patchValue({
        salt: UUID.UUID()
      });
    }

  }

  save() {
    if (this.id === 0) {
      this.insert();
    } else {
      this.update();
    }
  }

  private update() {
    this.tagService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Tag was successfully edited.');
      this.router.navigate(['tags']);
    });
  }

  private insert() {
    this.tagService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Tag was successfully created.');
      this.router.navigate(['tags']);
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
