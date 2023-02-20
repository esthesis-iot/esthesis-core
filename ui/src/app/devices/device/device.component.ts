import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {TagDto} from "../../tags/dto/tag-dto";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {TagsService} from "../../tags/tags.service";
import {DevicesService} from "../devices.service";
import {BaseComponent} from "src/app/shared/components/base-component";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {DeviceDto} from "../dto/device-dto";
import {AppConstants} from "../../app.constants";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-device",
  templateUrl: "./device.component.html",
  styleUrls: ["./device.component.scss"]
})
export class DeviceComponent extends BaseComponent implements OnInit {
  id!: string | null;
  deviceInfoForm!: FormGroup;
  device: DeviceDto | undefined;
  availableTags: TagDto[] | undefined;
  hardwareId = "";
  // Expose application constants.
  constants = AppConstants;
  // Geolocation URL for embedded Goggle Maps.
  geoUrl?: SafeResourceUrl;
  geoLastUpdated?: Date;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagService: TagsService,
    private devicesService: DevicesService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService,
    public sanitizer: DomSanitizer) {
    super();
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
    this.devicesService.getGeolocation(this.id!).subscribe({
      next: (geolocation) => {
        if (geolocation) {
          this.geoUrl = this.sanitizer.bypassSecurityTrustResourceUrl(`https://maps.google.com/maps?q=${geolocation.latitude},${geolocation.longitude}&z=13&output=embed`);
          this.geoLastUpdated = geolocation.lastUpdated;
        }
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch the gelocation for this device.", err);
      }
    });
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

  downloadPublicKey() {
    this.devicesService.downloadPublicKey(this.id!);
  }

  downloadPrivateKey() {
    this.devicesService.downloadPrivateKey(this.id!);
  }

  downloadCertificate() {
    this.devicesService.downloadCertificate(this.id!);
  }

  /**
   * Display a bigger version of the map in a dialog.
   */
  showMap() {
    // this.dialog.open(MapDialogComponent, {
    //   data: {
    //     geoUrl: this.geoUrl
    //   }
    // });
  }
}
