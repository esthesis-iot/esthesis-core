import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {BaseComponent} from '../shared/base-component';
import {DataSinkService} from './data-sink.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../shared/utility.service';
import {OkCancelModalComponent} from '../shared/display/ok-cancel-modal/ok-cancel-modal.component';
import {DataSinkFactoryDto} from '../dto/data-sink-factory-dto';
import * as _ from 'lodash';

@Component({
  selector: 'app-datasinks-edit',
  templateUrl: './datasinks-edit.component.html',
  styleUrls: ['./datasinks-edit.component.scss']
})
export class DatasinksEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  availableDataSinkFactories: DataSinkFactoryDto[];

  constructor(private fb: FormBuilder, private dataSinksService: DataSinkService, private qForms: QFormsService,
              private route: ActivatedRoute, private router: Router,
              private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.maxLength(256)]],
      factoryClass: ['', [Validators.maxLength(1024)]],
      metadata: ['', [Validators.maxLength(5)]],
      telemetry: ['', [Validators.maxLength(5)]],
      state: ['', [Validators.maxLength(5)]],
      configuration: ['', [Validators.maxLength(65535)]],
    });

    // Fill dropdowns.
    this.dataSinksService.getAvailableDataSinkFactories().subscribe(onNext => {
      this.availableDataSinkFactories = onNext;
    })
  }

  save() {
    this.dataSinksService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess(this.form.value.id ? 'Data sink was successfully saved.'
        : 'Data sink was successfully created.');
      this.router.navigate(['datasinks']);
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete data sink',
        question: 'Do you really want to delete this data sink?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.dataSinksService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Data sink successfully deleted.');
          this.router.navigate(['datasinks']);
        });
      }
    });
  }

  updateHandlers($event) {
    const factory = _.find<DataSinkFactoryDto>(this.availableDataSinkFactories, {factoryClass: $event.source.value});
    if (factory.supportsMetadata) {
      this.form.controls['metadata'].enable();
    } else {
      this.form.patchValue({
        metadata: false
      });
      this.form.controls['metadata'].disable();
    }

    if (factory.supportsTelemetry) {
      this.form.controls['telemetry'].enable();
    } else {
      this.form.patchValue({
        telemetry: false
      });
      this.form.controls['telemetry'].disable();
    }
  }
  
  template() {
    const factory = _.find<DataSinkFactoryDto>(this.availableDataSinkFactories, {factoryClass: this.form.controls['factoryClass'].value});
    console.log(factory);
    if (factory) {
      this.form.patchValue({
        configuration: factory.configurationTemplate
      })
    }
  }
}
