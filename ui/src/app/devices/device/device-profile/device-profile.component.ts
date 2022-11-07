import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {QFormsService} from "@qlack/forms";
import {DevicesService} from "../../devices.service";
import {BaseComponent} from "../../../shared/component/base-component";
import {ActivatedRoute} from "@angular/router";
import {UtilityService} from "../../../shared/service/utility.service";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {MatDialog} from "@angular/material/dialog";
import slugify from "slugify";
import {
  InputModalComponent
} from "../../../shared/component/display/input-modal/input-modal.component";
import {
  OkCancelModalComponent
} from "../../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";

@Component({
  selector: "app-device-profile",
  templateUrl: "./device-profile.component.html",
  styleUrls: ["./device-profile.component.scss"]
})
export class DeviceProfileComponent extends BaseComponent implements OnInit {
  id!: string | null;
  deviceProfileForm!: FormGroup;
  deviceProfileFormFields: FormlyFieldConfig[] = [];
  deviceProfileFormModel = {};

  constructor(private fb: FormBuilder, private qForms: QFormsService,
    private devicesService: DevicesService, private route: ActivatedRoute,
    private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the Device Profile form.
    this.deviceProfileForm = new FormGroup({});

    // Get Device profile notes.
    this.fetchNotes();
  }

  private fetchNotes(): void {
    this.deviceProfileFormModel = {};
    this.deviceProfileFormFields = [];
    this.devicesService.getDeviceProfileNotes(this.id!).subscribe({
      next: (deviceProfile) => {
        deviceProfile.forEach((field) => {
          this.deviceProfileFormFields = [...this.deviceProfileFormFields,
            {
              key: field.fieldName,
              defaultValue: field.fieldValue,
              type: "input",
              props: {
                label: field.label
              }
            }];
        });
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch device profile.", error);
      }
    });
  }

  private fetchDeviceProfileMeasurements(): void {

  }

  save() {
    this.devicesService.saveDeviceProfileNote(this.id!, this.deviceProfileFormModel).subscribe({
      next: () => {
        this.utilityService.popupSuccess("Device profile saved successfully.");
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not save device profile.", err);
      }
    });
  }

  add() {
    this.dialog.open(InputModalComponent, {
      data: {
        title: "Add new device note",
        question: "Enter the name of the new note to be added for this device:",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.devicesService.addDeviceProfileNote(
          this.id!, slugify(result).toLowerCase(), result).subscribe({
          next: () => {
            this.fetchNotes();
          }, error: (err) => {
            this.utilityService.popupErrorWithTraceId("Could not add device profile note.", err);
          }
        });
      }
    });
  }

  remove(keyName: any, label: string) {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Note",
        question: "Do you really want to delete '''" + label + "''' note?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.devicesService.removeDeviceProfileNote(this.id!, keyName).subscribe(onNext => {
          this.utilityService.popupSuccess("Device note successfully deleted.");
          this.fetchNotes();
        });
      }
    });

  }
}
