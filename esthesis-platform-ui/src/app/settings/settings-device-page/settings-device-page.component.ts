import {Component, OnInit} from '@angular/core';
import {FieldDto} from '../../dto/field-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../../shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';
import {QFormsService} from '@eurodyn/forms';
import {DevicesService} from '../../devices/devices.service';

@Component({
  selector: 'app-settings-device-page',
  templateUrl: './settings-device-page.component.html',
  styleUrls: ['./settings-device-page.component.scss']
})
export class SettingsDevicePageComponent extends BaseComponent implements OnInit {
  form: FormGroup;

  allFields: FieldDto[];

  constructor(private devicesService: DevicesService, private fb: FormBuilder,
              private utilityService: UtilityService, private qForms: QFormsService) {
    super();
  }

  ngOnInit() {
    this.form = this.fb.group({
      fields: this.fb.array([])
    });

    this.devicesService.getFields().subscribe(onNext => {
      this.allFields = onNext;
      onNext.forEach(field => {
        // @ts-ignore
        this.form.controls['fields'].push(this.createFieldElement(field));
      })
    })
  }

  createFieldElement(fieldDto: FieldDto) {
    return this.fb.group({
      name: [{value: fieldDto.name, disabled: true}, Validators.required],
      datatype: [{value: fieldDto.datatype, disabled: true}, Validators.required],
      shown: [fieldDto.shown],
      label: [fieldDto.label],
      datetime: [fieldDto.datetime],
      formatter: [fieldDto.formatter],
    });
  }

  save() {
    this.devicesService.saveFields(this.qForms.cleanupForm(this.form)['fields']).subscribe(
      onNext => {
        this.utilityService.popupSuccess("Settings saved successfully.");
      });
  }

  //TODO remove
  log(val) { console.log(val); }
}
