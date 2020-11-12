import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {BehaviorSubject} from 'rxjs';
import {BarcodeFormat} from '@zxing/library';

@Component({
  selector: 'app-device-preregister-cam',
  templateUrl: './device-preregister-cam.component.html',
  styleUrls: ['./device-preregister-cam.component.scss']
})
export class DevicePreregisterCamComponent implements OnInit {
  form: FormGroup;
  audioContext = AudioContext && new AudioContext();
  availableDevices: MediaDeviceInfo[];
  currentDevice: MediaDeviceInfo = null;
  formatsEnabled: BarcodeFormat[] = [
    BarcodeFormat.CODE_128,
    BarcodeFormat.DATA_MATRIX,
    BarcodeFormat.EAN_13,
    BarcodeFormat.QR_CODE,
  ];
  hasDevices: boolean;
  hasPermission: boolean;
  torchEnabled = false;
  torchAvailable$ = new BehaviorSubject<boolean>(false);
  tryHarder = false;

  constructor(private fb: FormBuilder,
              public selfDialogRef: MatDialogRef<DevicePreregisterCamComponent>) {
  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      ids: ['', []],
    });
  }

  onCamerasFound(devices: MediaDeviceInfo[]): void {
    this.availableDevices = devices;
    this.hasDevices = Boolean(devices && devices.length);
  }

  onCodeResult(resultString: string) {
    const existingIds: string = this.form.controls['ids'].value;
    if (!existingIds) {
      this.beepOK();
      this.form.controls['ids'].setValue(resultString);
    } else {
      if (existingIds.indexOf(resultString) === -1) {
        this.beepOK();
        this.form.controls['ids'].setValue(resultString + '\n' + existingIds);
      } else {
        this.beepExists();
      }
    }
  }

  onDeviceSelectChange(selected: string) {
    const device = this.availableDevices.find(x => x.deviceId === selected);
    this.currentDevice = device || null;
  }

  onHasPermission(has: boolean) {
    this.hasPermission = has;
  }

  onTorchCompatible(isCompatible: boolean): void {
    this.torchAvailable$.next(isCompatible || false);
  }

  toggleTorch(): void {
    this.torchEnabled = !this.torchEnabled;
  }

  toggleTryHarder(): void {
    this.tryHarder = !this.tryHarder;
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
    v.stop(this.audioContext.currentTime + 0.1);
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
