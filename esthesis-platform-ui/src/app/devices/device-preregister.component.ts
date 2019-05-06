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
import {TagDto} from '../dto/tag-dto';
import {TagService} from '../tags/tag.service';
import {UUID} from 'angular2-uuid';
import {ContainerDeployComponent} from '../shared/containers/container-deploy.component';
import {DevicePreregisterCamComponent} from './device-preregister-cam.component';
import {MatDialog} from '@angular/material';

@Component({
  selector: 'app-device-preregister',
  templateUrl: './device-preregister.component.html',
  styleUrls: ['./device-preregister.component.scss']
})
export class DevicePreregisterComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  availableTags: TagDto[];

  constructor(private fb: FormBuilder, private qForms: QFormsService,
              private devicesService: DevicesService, private router: Router,
              private utilityService: UtilityService, private tagService: TagService,
              private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      ids: ['', [Validators.required]],
      tags: [[]]
    });

    // Get available tags.
    this.tagService.getAll().subscribe(onNext => {
      this.availableTags = onNext.content;
    });
  }

  save() {
    this.devicesService.preregister(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('Devices are registering... Please refresh devices list.');
      this.router.navigate(['devices']);
    });
  }

  useCam() {
    // Create a random topic to monitor this deployment via WebSockets.
    const wsId = UUID.UUID();
    const dialogRef = this.dialog.open(DevicePreregisterCamComponent, {
      height: '70%',
      width: '80%',
      data: {
        wsId: wsId
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.form.controls['ids'].patchValue(result);
      }
    });
  }
}
