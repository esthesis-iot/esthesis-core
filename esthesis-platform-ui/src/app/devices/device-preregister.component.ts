import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../shared/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {QFormsService} from '@eurodyn/forms';
import {DevicesService} from './devices.service';
import {Router} from '@angular/router';
import {ZXingScannerComponent} from '@zxing/ngx-scanner';
import {KeyValueDto} from '../dto/key-value-dto';
import * as _ from 'lodash';
import {Log} from 'ng2-logger/browser';
import {UtilityService} from '../shared/utility.service';

@Component({
  selector: 'app-device-preregister',
  templateUrl: './device-preregister.component.html',
  styleUrls: ['./device-preregister.component.scss']
})
export class DevicePreregisterComponent extends BaseComponent implements OnInit {
  private log = Log.create('DevicePreregisterComponent');
  form: FormGroup;
  @ViewChild('scanner')
  scanner: ZXingScannerComponent;
  cameras: KeyValueDto[] = [];
  camControls = false;
  selectedCam: string;

  constructor(private fb: FormBuilder, private qForms: QFormsService, private devicesService: DevicesService,
              private router: Router, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      ids: ['', [Validators.required]]
    });
  }

  save() {
    this.devicesService.preregister(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Devices were successfully registered.');
      this.router.navigate(['devices']);
    });
  }

  scanCompleteHandler(event) {
    const existingIds: string = this.form.controls['ids'].value;
    const newId = event['text'];
    if (!existingIds) {
      this.form.controls['ids'].setValue(newId);
    } else {
      if (existingIds.indexOf(newId) === -1) {
        this.form.controls['ids'].setValue(newId + '\n' + existingIds);
      }
    }
  }

  camerasFoundHandler(event) {
    this.cameras = [];
    for (const cam of _.orderBy(event, ['label'], ['asc'])) {
      this.cameras.push(new KeyValueDto(cam['deviceId'], cam['label']));
    }
    if (this.cameras.length > 0) {
      this.selectedCam = this.cameras[0].key;
      this.scanner.scan(this.selectedCam);
    }
  }

  changeCamera(cameraId: string) {
    this.scanner.scan(cameraId);
  }

  switchCamControls() {
    this.camControls = !this.camControls;
  }
}
