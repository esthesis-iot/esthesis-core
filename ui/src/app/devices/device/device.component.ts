import {AfterViewInit, Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {TagDto} from "../../dto/tag-dto";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {TagService} from "../../tags/tag.service";
import {DevicesService} from "../devices.service";
import {BaseComponent} from "src/app/shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {SettingsService} from "../../settings/settings.service";
import {QFormsService} from "@qlack/forms";
import {DeviceDto} from "../../dto/device-dto";
import {AppConstants} from "../../app.constants";
import {DevicePageFieldDataDto} from "../../dto/device-page-field-data-dto";

@Component({
  selector: "app-device",
  templateUrl: "./device.component.html",
  styleUrls: ["./device.component.scss"]
})
export class DeviceComponent extends BaseComponent implements OnInit, AfterViewInit {
  id!: string | null;
  deviceInfoForm!: FormGroup;

  device: DeviceDto | undefined;

  availableTags: TagDto[] | undefined;
  fields!: DevicePageFieldDataDto[];
  fieldsValues!: Map<string, any>;
  hardwareId = "";
  // Expose application constants.
  constants = AppConstants;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagService: TagService,
    private devicesService: DevicesService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService,
    private settingsService: SettingsService) {
    super();
  }

  ngAfterViewInit(): void {
    // If viewing an existing device, fetch data for it.
    // if (this.id && this.id !== this.constants.NEW_RECORD_ID) {
    //   let latSetting: string;
    //   let lonSetting: string;
    //   this.settingsService.findByNames("DEVICE_GEO_LAT,DEVICE_GEO_LON").subscribe(onNext => {
    //     onNext.forEach(registryEntryDTO => {
    //       if (registryEntryDTO.name === "DEVICE_GEO_LAT") {
    //         latSetting = registryEntryDTO.value;
    //       }
    //       if (registryEntryDTO.name === "DEVICE_GEO_LON") {
    //         lonSetting = registryEntryDTO.value;
    //       }
    //     });
    //   });
    // }
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the Device Info form.
    this.deviceInfoForm = this.fb.group({
      id: [""],
      tags: [[]],
      status: ["", [Validators.required, Validators.maxLength(32)]],
      hardwareId: ["", [Validators.required, Validators.maxLength(512)]]
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Check if an edit is performed and fetch the data for this device.
    if (this.id && this.id !== this.constants.NEW_RECORD_ID) {
      this.devicesService.findById(this.id).subscribe(onNext => {
        this.device = onNext;
        this.deviceInfoForm.patchValue(onNext);
        this.hardwareId = onNext.hardwareId;
      });
    }

    this.updateTelemetryMetadata();
  }

  private updateTelemetryMetadata() {
    this.fieldsValues = new Map<string, any>();
    // this.devicesService.getDevicePageData(this.id!).subscribe(fieldsValues => {
    //   this.fields = fieldsValues;
    //   // Update field values formatting.
    //   this.fields!.forEach(field => {
    //     let formatter;
    //     if (!field.formatter) {
    //       formatter = "%s";
    //     } else {
    //       formatter = field.formatter;
    //     }
    //     let value;
    //     if (field.valueHandler) {
    //       value = this.formatterService.format(field.valueHandler, field.value);
    //     } else {
    //       value = field.value;
    //     }
    //     this.fieldsValues!.set(field.measurement + "." + field.field, sprintf(formatter, value));
    //   });
    //   this.fetchingDeviceData = false;
    // });
  }

  saveDeviceInfo() {
    this.devicesService.save(
      this.qForms.cleanupData(this.deviceInfoForm.getRawValue()) as DeviceDto).subscribe(
      onNext => {
        this.utilityService.popupSuccess("Device successfully saved.");
        this.router.navigate(["devices"]);
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
        this.devicesService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess("Device deletion request successfully submitted and" +
            " is ongoing.");
          this.router.navigate(["devices"]);
        });
      }
    });
  }

  getLastUpdatedDate(date: Date): string {
    if (!(date instanceof Date)) {
      date = new Date(date);
    }

    return new Date(date).toLocaleString();
  }

  downloadKeys() {
    this.devicesService.downloadKeys(this.id!);
  }

}
