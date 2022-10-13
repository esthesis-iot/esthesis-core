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
import {sprintf} from "sprintf-js";
import {FieldDto} from "../../dto/field-dto";
import {FormatterService} from "../../shared/service/formatter.service";
import {SettingsService} from "../../settings/settings.service";
import {QFormsService} from "@qlack/forms";
import {DeviceDto} from "../../dto/device-dto";
import {AppConstants} from "../../app.constants";
import {NiFiService} from "../../infrastructure/infrastructure-nifi/nifi.service";

@Component({
  selector: "app-device",
  templateUrl: "./device.component.html",
  styleUrls: ["./device.component.scss"]
})
export class DeviceComponent extends BaseComponent implements OnInit, AfterViewInit {
  availableTags: TagDto[] | undefined;
  form!: FormGroup;
  id!: number;
  fields!: FieldDto[];
  fieldsValues!: Map<string, any>;
  fetchingDeviceData = true;
  deviceHasGeolocation = false;
  hardwareId = "";
  // Expose application constants.
  constants = AppConstants;
  isNiFiConfigured = false;
  hasActiveNiFi = false;
  hasDTUrl = false;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagService: TagService,
    private devicesService: DevicesService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService,
    private formatterService: FormatterService,
    private settingsService: SettingsService, private nifiService: NiFiService) {
    super();
  }

  ngAfterViewInit(): void {
    // If viewing an existing device, fetch data for it.
    if (this.id && this.id !== 0) {
      let latSetting: string;
      let lonSetting: string;
      this.settingsService.findByNames("DEVICE_GEO_LAT,DEVICE_GEO_LON").subscribe(onNext => {
        onNext.forEach(registryEntryDTO => {
          if (registryEntryDTO.name === "DEVICE_GEO_LAT") {
            latSetting = registryEntryDTO.value;
          }
          if (registryEntryDTO.name === "DEVICE_GEO_LON") {
            lonSetting = registryEntryDTO.value;
          }
        });
      });
    }
  }

  ngOnInit() {

    this.nifiService.getActive().subscribe(value => {
      this.hasActiveNiFi = value?.id != null;
      this.hasDTUrl = value?.dtUrl != null;
      this.isNiFiConfigured = this.hasActiveNiFi && this.hasDTUrl;
    });

    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get("id"));

    // Setup the form.
    this.form = this.fb.group({
      id: [""],
      tags: [[]],
      state: ["", [Validators.required, Validators.maxLength(32)]],
      hardwareId: ["", [Validators.required, Validators.maxLength(512)]]
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    if (this.id && this.id !== 0) {
      this.devicesService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
        this.hardwareId = onNext.hardwareId;
      });
    }

    this.updateTelemetryMetadata();
  }

  private updateTelemetryMetadata() {
    this.fieldsValues = new Map<string, any>();
    this.devicesService.getDevicePageData(this.id!).subscribe(fieldsValues => {
      this.fields = fieldsValues;
      // Update field values formatting.
      this.fields!.forEach(field => {
        let formatter;
        if (!field.formatter) {
          formatter = "%s";
        } else {
          formatter = field.formatter;
        }
        let value;
        if (field.valueHandler) {
          value = this.formatterService.format(field.valueHandler, field.value);
        } else {
          value = field.value;
        }
        this.fieldsValues!.set(field.measurement + "." + field.field, sprintf(formatter, value));
      });
      this.fetchingDeviceData = false;
    });
  }

  save() {
    this.devicesService.save(
      this.qForms.cleanupData(this.form.getRawValue()) as DeviceDto).subscribe(
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
