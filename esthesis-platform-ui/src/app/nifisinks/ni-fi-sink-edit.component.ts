import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {QFormsService} from '@eurodyn/forms';
import {NifiSinkService} from './nifi-sink.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';
import {OkCancelModalComponent} from '../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {NifiReaderFactoryDto} from '../dto/nifisinks/nifi-reader-factory-dto';
import {NiFiProducerFactoryDto} from '../dto/nifisinks/ni-fi-producer-factory-dto';
import * as _ from 'lodash';
import {NiFiWriterFactoryDto} from '../dto/nifisinks/nifi-writer-factory-dto';
import {NiFiLoggerFactoryDto} from '../dto/nifisinks/nifi-logger-factory-dto';
import {AppConstants} from '../app.constants';

@Component({
  selector: 'app-nifisink-edit',
  templateUrl: './ni-fi-sink-edit.component.html',
  styleUrls: ['./ni-fi-sink-edit.component.scss']
})
export class NiFiSinkEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  availableNiFiDataFactories: any;
  type: string
  handlers = Object.values(AppConstants.HANDLER);
  isEdit: boolean;

  constructor(private fb: FormBuilder, private nifDataService: NifiSinkService,
              private qForms: QFormsService,
              private route: ActivatedRoute, private router: Router,
              private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.isEdit = this.id != 0;

    let strings = this.router.url.split("/");
    this.type = strings[1];

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: [{value: '', disabled: this.isEdit}, [Validators.required, Validators.maxLength(256)]],
      factoryClass: [{value: '', disabled: this.isEdit}, [Validators.required, Validators.maxLength(
        1024)]],
      handler: [{value: '', disabled: true}, [Validators.required, Validators.maxLength(1024)]],
      state: ['', [Validators.maxLength(5)]],
      configuration: ['', [Validators.maxLength(65535)]],
      type: ['', [Validators.maxLength(1024)]],
      processorId: ['']
    });

    // Fill-in the form with data if editing an existing item.
    if (this.isEdit) {
      this.nifDataService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // Fill dropdowns.
    this.nifDataService.getAvailableDataFactoriesByType(this.type).subscribe(onNext => {
      this.availableNiFiDataFactories = onNext;
    })
  }

  save() {
    this.form.patchValue({
      type: this.type
    });
    this.nifDataService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess(this.form.value.id ? 'NiFi sink was successfully saved.'
        : 'NiFi sink was successfully created.');
      this.router.navigate([this.type]);
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
        this.nifDataService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Data sink successfully deleted.');
          this.router.navigate([this.type]);
        });
      }
    });
  }

  updateHandlers($event) {
    let nifiSinkTypeDTO = this.getNifiSinkTypeDTO();
    const factory = _.find<typeof nifiSinkTypeDTO>(this.availableNiFiDataFactories,
      {factoryClass: $event.source.value});

    //Keeping only supported handlers in edit mode.
    if (!this.isEdit) {
      //Action is derived from the type (read, write, produce).
      let action = this.type.charAt(0).toUpperCase() + this.type.slice(1,
        this.type == "readers" ? -3 : -2);
      if (this.type != "loggers") {
        if (!factory["supportsTelemetry" + action]) {
          this.handlers.pop();
        }
        if (!factory["supportsMetadata" + action]) {
          this.handlers.pop();
        }
        if ((this.type == "writers") || !factory["supportsPing" + action]) {
          this.handlers.shift();
        }
      }
      this.form.get('handler').enable();
    }
  }

  template() {
    let nifiSinkTypeDTO = this.getNifiSinkTypeDTO();
    const factory = _.find<typeof nifiSinkTypeDTO>(this.availableNiFiDataFactories,
      {factoryClass: this.form.controls['factoryClass'].value});
    if (factory) {
      this.form.patchValue({
        configuration: factory.configurationTemplate
      })
    }
  }

  private getNifiSinkTypeDTO(): any {
    switch (this.type) {
      case "readers":
        return NifiReaderFactoryDto;
      case "writers" :
        return NiFiWriterFactoryDto;
      case "producers" :
        return NiFiProducerFactoryDto;
      default:
        return NiFiLoggerFactoryDto;
    }
  }
}
