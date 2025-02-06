import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DevicesService} from "../devices.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {DeviceDto} from "../dto/device-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {GeolocationDto} from "../dto/geolocation-dto";
import {FramedMapComponent} from "../../shared/components/framed-map/framed-map.component";
import {AppConstants} from "../../app.constants";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {latLng, Layer, MapOptions, marker, tileLayer} from "leaflet";

@Component({
  selector: "app-device",
  templateUrl: "./device.component.html"
})
export class DeviceComponent extends SecurityBaseComponent implements OnInit {
  id!: string | null;
  deviceInfoForm!: FormGroup;
  device: DeviceDto | undefined;
  hardwareId = "";
  // Geolocation and map options for this device.
  geolocation?: GeolocationDto;
  mapOptions?: MapOptions;
  mapLayers?: Layer[];

  constructor(private readonly fb: FormBuilder, private readonly dialog: MatDialog,
    private readonly devicesService: DevicesService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the Device Info form.
    this.deviceInfoForm = this.fb.group({
      id: [],
      tags: [[]],
      status: [null, [Validators.required, Validators.maxLength(32)]],
      hardwareId: [null, [Validators.required, Validators.maxLength(512)]]
    });
    this.isFormDisabled().subscribe(disabled => disabled && this.deviceInfoForm.disable());

    // Fetch the data for this device.
    this.devicesService.findById(this.id).subscribe(onNext => {
      this.device = onNext;
      this.deviceInfoForm.patchValue(onNext);
      this.hardwareId = onNext.hardwareId;
    });

    // Get geolocation if available.
    this.devicesService.getGeolocation(this.id!).subscribe({
      next: (geolocation) => {
        this.geolocation = geolocation;
        this.mapOptions = {
          layers: [
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
          ],
          zoom: 15,
          center: latLng(geolocation.latitude, geolocation.longitude)
        };
        this.mapLayers = [
          marker([geolocation.latitude, geolocation.longitude],
            { icon: AppConstants.MAP_DEFAULT_ICON})
        ];
      }
    })
  }

  saveDeviceInfo() {
    this.devicesService.saveTagsAndStatus(this.deviceInfoForm.getRawValue() as DeviceDto).subscribe({
      next: () => {
        this.utilityService.popupSuccess("Device successfully saved.");
        this.router.navigate(["devices"]);
      }
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Device",
        question: "Do you really want to delete this Device?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.devicesService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Device deletion request successfully submitted and" +
              " is ongoing.");
            this.router.navigate(["devices"]);
          }
        });
      }
    });
  }

  downloadCertificate() {
    this.devicesService.download(this.id!, AppConstants.KEY_TYPE.CERTIFICATE);
  }

  downloadPublicKey() {
    this.devicesService.download(this.id!, AppConstants.KEY_TYPE.PUBLIC_KEY);
  }

  downloadPrivateKey() {
    this.devicesService.download(this.id!, AppConstants.KEY_TYPE.PRIVATE_KEY);
  }

  /**
   * Display a bigger version of the map in a dialog.
   */
  showMap() {
    this.dialog.open(FramedMapComponent, {
      maxWidth: "100vw",
      maxHeight: "100vh",
      width: "100%",
      height: "100%",
      data: {
        longitude: this.geolocation!.longitude,
        latitude: this.geolocation!.latitude,
        zoom: 15,
        title: "Device " + this.hardwareId
      }
    });
  }

}
