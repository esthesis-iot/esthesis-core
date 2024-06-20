import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {TagDto} from "../../tags/dto/tag-dto";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {TagsService} from "../../tags/tags.service";
import {DevicesService} from "../devices.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {DeviceDto} from "../dto/device-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {GeolocationDto} from "../dto/geolocation-dto";
import {Observable} from "rxjs";
import {FramedMapComponent} from "../../shared/components/framed-map/framed-map.component";
import {AppConstants} from "../../app.constants";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-device",
  templateUrl: "./device.component.html"
})
export class DeviceComponent extends SecurityBaseComponent implements OnInit {
  id!: string | null;
  deviceInfoForm!: FormGroup;
  device: DeviceDto | undefined;
  availableTags: TagDto[] | undefined;
  hardwareId = "";
  // Geolocation details for this device.
  geolocation?: Observable<GeolocationDto>;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagService: TagsService,
    private devicesService: DevicesService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService) {
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

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Fetch the data for this device.
    this.devicesService.findById(this.id).subscribe(onNext => {
      this.device = onNext;
      this.deviceInfoForm.patchValue(onNext);
      this.hardwareId = onNext.hardwareId;
    });

    // Get geolocation if available.
    this.geolocation = this.devicesService.getGeolocation(this.id!);

    // Set security.

  }

  saveDeviceInfo() {
    this.devicesService.save(this.deviceInfoForm.getRawValue() as DeviceDto).subscribe({
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
    this.devicesService.getGeolocation(this.id!).subscribe({
      next: (geolocation) => {
        const mapInstance = this.dialog.open(FramedMapComponent, {
          maxWidth: "100vw",
          maxHeight: "100vh",
          height: "100%",
          width: "100%"
        }).componentInstance;
        mapInstance.longitude = geolocation.longitude;
        mapInstance.latitude = geolocation.latitude;
        mapInstance.zoom = 13;
        mapInstance.height = "100%";
        mapInstance.width = "100%";
        mapInstance.title = "Device " + this.hardwareId;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId(error, "Error while retrieving geolocation.");
      }
    });
  }
}
