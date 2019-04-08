import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {icon, latLng, marker, tileLayer} from 'leaflet';
import {TagDto} from '../../dto/tag-dto';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {TagService} from '../../tags/tag.service';
import {MqttServerService} from '../../infrastructure/infrastructure-mqtt/mqtt-server.service';
import {UtilityService} from '../../shared/utility.service';
import {CertificatesService} from '../../certificates/certificates.service';
import {CasService} from '../../cas/cas.service';
import {DevicesService} from '../devices.service';
import {BaseComponent} from '../../shared/base-component';

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
      tags: [[]]
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

}
