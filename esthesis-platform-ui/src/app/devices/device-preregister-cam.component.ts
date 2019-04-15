import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ZXingScannerComponent} from '@zxing/ngx-scanner';
import {KeyValueDto} from '../dto/key-value-dto';
import * as _ from 'lodash';
import {MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-device-preregister-cam',
  templateUrl: './device-preregister-cam.component.html',
  styleUrls: ['./device-preregister-cam.component.scss']
})
export class DevicePreregisterCamComponent implements OnInit {
  form: FormGroup;
  @ViewChild('scanner')
  scanner: ZXingScannerComponent;
  cameras: KeyValueDto[] = [];
  selectedCam: string;
  private audioContext = new AudioContext();
  // A list of strings for cams to be ignored.
  private ignoreCams = ['CamTwist'];

  constructor(private fb: FormBuilder, public selfDialogRef: MatDialogRef<DevicePreregisterCamComponent>) {

  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      ids: ['', []],
    });
  }

  scanCompleteHandler(event) {
    const existingIds: string = this.form.controls['ids'].value;
    const newId = event['text'];
    if (!existingIds) {
      this.beepOK();
      this.form.controls['ids'].setValue(newId);
    } else {
      if (existingIds.indexOf(newId) === -1) {
        this.beepOK();
        this.form.controls['ids'].setValue(newId + '\n' + existingIds);
      } else {
        this.beepExists();
      }
    }
  }

  camerasFoundHandler(event) {
    this.cameras = [];
    for (const cam of _.orderBy(event, ['label'], ['asc'])) {
      // Ignore camera names that should be ignored.
      if (!this.ignoreCams.find(o => {
        return cam['label'].toLowerCase().indexOf(o.toLowerCase()) > -1
      })) {
        this.cameras.push(new KeyValueDto(cam['deviceId'], cam['label']));
      }
    }
    if (this.cameras.length > 0) {
      this.selectedCam = this.cameras[0].key;
      this.scanner.scan(this.selectedCam);
    }
  }

  changeCamera(cameraId: string) {
    this.scanner.scan(cameraId);
  }

  save() {
    this.selfDialogRef.close(this.form.controls['ids'].value);
  }

  private beep(frequency: number) {
    let v = this.audioContext.createOscillator();
    let u = this.audioContext.createGain();
    v.connect(u);
    v.frequency.value = frequency;
    v.type = "square";
    u.connect(this.audioContext.destination);
    u.gain.value = 0.1;
    v.start(this.audioContext.currentTime);
    v.stop(this.audioContext.currentTime + 100 * 0.001);
  }

  beepExists() {
    this.beep(220);
  }

  beepOK() {
    this.beep(440);
  }

  close() {
    this.selfDialogRef.close();
  }
}
