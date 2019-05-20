import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {icon, latLng, marker, tileLayer} from 'leaflet';
import {TagDto} from '../../dto/tag-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {TagService} from '../../tags/tag.service';
import {UtilityService} from '../../shared/utility.service';
import {DevicesService} from '../devices.service';
import {BaseComponent} from '../../shared/base-component';
import {OkCancelModalComponent} from '../../shared/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.scss']
})
export class DeviceComponent extends BaseComponent implements OnInit {
  availableTags: TagDto[];
  form: FormGroup;
  id: number;

  options = {
    layers: [
      tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18})
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
              private router: Router, private utilityService: UtilityService) {
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

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.devicesService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    // Get available tags.
    this.tagService.getAll().subscribe(onNext => {
      this.availableTags = onNext.content;
    });
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
