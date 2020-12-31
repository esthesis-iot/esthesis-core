import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {DataSinkService} from './data-sink.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {DataSinkFactoryDto} from '../dto/data-sink-factory-dto';
import * as _ from 'lodash';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';
import {OkCancelModalComponent} from '../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {DataSinkDto} from '../dto/data-sink-dto';
import {QFormsService} from '@qlack/forms';

@Component({
  selector: 'app-datasinks-edit',
  templateUrl: './datasinks-edit.component.html',
  styleUrls: ['./datasinks-edit.component.scss']
})
export class DatasinksEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: number;
  availableDataSinkFactories!: DataSinkFactoryDto[];

  constructor(private fb: FormBuilder, private dataSinksService: DataSinkService,
              private qForms: QFormsService,
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
      metadataRead: ['', [Validators.maxLength(5)]],
      telemetryRead: ['', [Validators.maxLength(5)]],
      metadataWrite: ['', [Validators.maxLength(5)]],
      telemetryWrite: ['', [Validators.maxLength(5)]],
      state: ['', [Validators.maxLength(5)]],
      configuration: ['', [Validators.maxLength(65535)]],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.dataSinksService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // Fill dropdowns.
    this.dataSinksService.getAvailableDataSinkFactories().subscribe(onNext => {
      this.availableDataSinkFactories = onNext;
    })
  }

  save() {
    this.dataSinksService.save(
      this.qForms.cleanupData(this.form.getRawValue()) as DataSinkDto).subscribe(onNext => {
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

  updateHandlers($event: any) {
    const factory = _.find<DataSinkFactoryDto>(this.availableDataSinkFactories,
      {factoryClass: $event.source.value});
    if (factory) {
      if (factory.supportsMetadataRead) {
        this.form.controls['metadataRead'].enable();
      } else {
        this.form.patchValue({
          metadata: false
        });
        this.form.controls['metadataRead'].disable();
      }

      if (factory.supportsTelemetryRead) {
        this.form.controls['telemetryRead'].enable();
      } else {
        this.form.patchValue({
          telemetry: false
        });
        this.form.controls['telemetryRead'].disable();
      }
      if (factory.supportsMetadataWrite) {
        this.form.controls['metadataWrite'].enable();
      } else {
        this.form.patchValue({
          metadata: false
        });
        this.form.controls['metadataWrite'].disable();
      }

      if (factory.supportsTelemetryWrite) {
        this.form.controls['telemetryWrite'].enable();
      } else {
        this.form.patchValue({
          telemetry: false
        });
        this.form.controls['telemetryWrite'].disable();
      }
    }
  }

  template() {
    const factory = _.find<DataSinkFactoryDto>(this.availableDataSinkFactories,
      {factoryClass: this.form.controls['factoryClass'].value});
    console.log(factory);
    if (factory) {
      this.form.patchValue({
        configuration: factory.configurationTemplate
      })
    }
  }
}
