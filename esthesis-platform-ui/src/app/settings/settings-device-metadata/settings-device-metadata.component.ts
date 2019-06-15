import {Component, OnInit} from '@angular/core';
import {FieldDto} from '../../dto/field-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../../shared/component/base-component';
import {DeviceMetadataService} from '../../shared/service/device-metadata.service';
import {UtilityService} from '../../shared/service/utility.service';
import {QFormsService} from '@eurodyn/forms';

@Component({
  selector: 'app-settings-device-metadata',
  templateUrl: './settings-device-metadata.component.html',
  styleUrls: ['./settings-device-metadata.component.scss']
})
export class SettingsDeviceMetadataComponent extends BaseComponent implements OnInit {
  form: FormGroup;

  allFields: FieldDto[];

  constructor(private deviceMetadataService: DeviceMetadataService, private fb: FormBuilder,
              private utilityService: UtilityService, private qForms: QFormsService) {
    super();
  }

  ngOnInit() {
    this.form = this.fb.group({
      fields: this.fb.array([])
    });

    this.deviceMetadataService.getMetadataFields().subscribe(onNext => {
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
      label: [fieldDto.label]
    });
  }

  save() {
    this.deviceMetadataService.save(this.qForms.cleanupForm(this.form)['fields']).subscribe(
      onNext => {
        this.utilityService.popupSuccess("Settings saved successfully.");
      });
  }
}
