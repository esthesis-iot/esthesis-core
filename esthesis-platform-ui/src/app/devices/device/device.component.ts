import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {icon, latLng, marker, tileLayer} from 'leaflet';
import {TagDto} from '../../dto/tag-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {QFormsService} from '@eurodyn/forms';
import {TagService} from '../../tags/tag.service';
import {DevicesService} from '../devices.service';
import {BaseComponent} from 'src/app/shared/component/base-component';
import {UtilityService} from '../../shared/service/utility.service';
import {OkCancelModalComponent} from '../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {sprintf} from 'sprintf-js';
import {FieldDto} from '../../dto/field-dto';
import {FormatterService} from '../../shared/service/formatter.service';
import {SettingsService} from '../../settings/settings.service';
import {AppSettings} from '../../app.settings';

@Component({
  selector: 'app-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.scss']
})
export class DeviceComponent extends BaseComponent implements OnInit {
  availableTags: TagDto[];
  form: FormGroup;
  id: number;
  fields: FieldDto[];
  fieldsValues: Map<string, any>;
  lonSetting: string;
  latSetting: string;

  options = {
    layers: [
      tileLayer(window.location.protocol + '//{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18})
    ],
    zoom: 14,
    center: latLng(47.2287109, 14.3009642)
  };

  layer1 = [
    marker([47.2287109, 14.3009642], {
      icon: icon({
        iconSize: [25, 41],
        iconAnchor: [13, 41],
        iconUrl: 'assets/marker-icon.png',
        shadowUrl: 'assets/marker-shadow.png'
      })
    }),
  ];

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService, private tagService: TagService,
              private devicesService: DevicesService, private route: ActivatedRoute,
              private router: Router, private utilityService: UtilityService,
              private formatterService: FormatterService,
              private settingsService: SettingsService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      tags: [[]],
      state: ['', [Validators.required, Validators.maxLength(32)]],
      hardwareId: ['', [Validators.required, Validators.maxLength(512)]]
    });

    // If viewing an existing device, fetch data for it.
    if (this.id && this.id !== 0) {
      this.devicesService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
      this.updateFields();
    }

    // Get available tags.
    this.tagService.getAll().subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Get lat/lon parameters.
    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.GEOLOCATION.LATITUDE,
      AppSettings.SETTING.GEOLOCATION.LONGITUDE,
    ).subscribe(onNext => {
      onNext.forEach(settingDTO => {
        if (settingDTO.key === AppSettings.SETTING.GEOLOCATION.LATITUDE) {
          this.latSetting = settingDTO.val;
        }
        if (settingDTO.key === AppSettings.SETTING.GEOLOCATION.LONGITUDE) {
          this.lonSetting = settingDTO.val;
        }
      });
    });
  }

  private updateFields() {
    // this.fieldsValues = new Map<string, any>();
    // this.devicesService.getFieldValues(this.id).subscribe(fieldsValues => {
    //   this.fields = fieldsValues;
    //   // Update field values formatting.
    //   this.fields.forEach(field => {
    //     var formatter;
    //     if (!field.formatter) {
    //       formatter = "%s";
    //     } else {
    //       formatter = field.formatter;
    //     }
    //     var value;
    //     if (field.valueHandler) {
    //       value = this.formatterService.format(field.valueHandler, field.value);
    //     } else {
    //       value = field.value;
    //     }
    //     this.fieldsValues.set(field.name, sprintf(formatter, value));
    //   })
    // });
  }

  save() {
    this.devicesService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Device successfully saved.');
      this.router.navigate(['devices']);
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete Device',
        question: 'Do you really want to delete this Device?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.devicesService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Device deletion request successfully submitted and' +
            ' is ongoing.');
          this.router.navigate(['devices']);
        });
      }
    });
  }

  downloadKeys() {
    this.devicesService.downloadKeys(this.id);
  }

}
