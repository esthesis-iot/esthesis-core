import {Component, OnInit} from '@angular/core';
import {SettingsService} from '../settings.service';
import {FieldDto} from '../../dto/field-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../../shared/component/base-component';

@Component({
  selector: 'app-settings-device-metadata',
  templateUrl: './settings-device-metadata.component.html',
  styleUrls: ['./settings-device-metadata.component.scss']
})
export class SettingsDeviceMetadataComponent extends BaseComponent implements OnInit {
  form: FormGroup;

  allFields: FieldDto[];

  constructor(private settingService: SettingsService, private fb: FormBuilder) {
    super();
  }

  ngOnInit() {
    this.form = this.fb.group({
      fields: this.fb.array([])
    });

    this.settingService.getMetadataFields().subscribe(onNext => {
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
      shown: [fieldDto.shown, Validators.required],
      label: ['']
    });
  }

  // save() {
  //   this.caService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
  //     this.utilityService.popupSuccess(this.form.value.id ? 'Certificate authority was
  // successfully edited.' : 'Certificate authority was successfully created.');
  // this.router.navigate(['cas']); }); }
}
