import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {QFormsService} from "@qlack/forms";
import {DevicesService} from "../../devices.service";
import {BaseComponent} from "../../../shared/components/base-component";
import {ActivatedRoute} from "@angular/router";
import {FormlyFieldConfig} from "@ngx-formly/core";
import slugify from "slugify";
import {InputModalComponent} from "../../../shared/components/input-modal/input-modal.component";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {DevicePageFieldDataDto} from "../../dto/device-page-field-data-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../../shared/services/utility.service";

@Component({
  selector: "app-device-profile",
  templateUrl: "./device-profile.component.html",
  styleUrls: ["./device-profile.component.scss"]
})
export class DeviceProfileComponent extends BaseComponent implements OnInit {
  id!: string | null;
  // The form representing all device profile attributes.
  deviceProfileForm!: FormGroup;

  // The list of Formly entries representing device attributes.
  deviceAttributesFormFields: FormlyFieldConfig[] = [];

  // The model representation of all device profile attributes.
  deviceAttributesModel = {};

  // The list of device profile fields (i.e. user-defined list of device attributes to be
  // displayed in boxes on ton of the screen).
  dataFields: DevicePageFieldDataDto[] = [];

  constructor(private fb: FormBuilder, private qForms: QFormsService,
    private devicesService: DevicesService, private route: ActivatedRoute,
    private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the Device Profile form.
    this.deviceProfileForm = new FormGroup({});

    // Get Device profile attributes.
    this.fetchAttributes();

    // Get Device profile fields.
    this.fetchFields();
  }

  /**
   * Fetch the list of user-defined fields to be displayed on the device profile page.
   */
  private fetchFields(): void {
    this.devicesService.getProfileFieldsData(this.id!).subscribe({
      next: (fields) => {
        this.dataFields = fields;
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch device profile fields.", err);
      }
    });
  }

  /**
   * Fetch the list of device profile attributes.
   * @private
   */
  private fetchAttributes(): void {
    this.deviceAttributesModel = {};
    this.deviceAttributesFormFields = [];
    this.devicesService.getDeviceAttributes(this.id!).subscribe({
      next: (deviceProfile) => {
        deviceProfile.forEach((field) => {
          this.deviceAttributesFormFields = [...this.deviceAttributesFormFields,
            {
              key: field.attributeName,
              defaultValue: field.attributeValue,
              type: "input",
              props: {
                label: field.attributeName
              }
            }];
        });
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch device profile.", error);
      }
    });
  }

  /**
   * Save the device profile attributes.
   */
  save() {
    this.devicesService.saveDeviceAttributes(this.id!, this.deviceAttributesModel).subscribe({
      next: () => {
        this.utilityService.popupSuccess("Device profile saved successfully.");
        this.deviceProfileForm.markAsPristine();
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not save device profile.", err);
      }
    });
  }

  /**
   * Add a new device profile attribute.
   */
  add() {
    this.dialog.open(InputModalComponent, {
      data: {
        title: "Add new device attribute",
        question: "Enter the name of the new attribute to be added for this device:",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.devicesService.addDeviceAttribute(
          this.id!, slugify(result).toLowerCase(), result).subscribe({
          next: () => {
            this.fetchAttributes();
          }, error: (err) => {
            this.utilityService.popupErrorWithTraceId("Could not add device profile attribute.", err);
          }
        });
      }
    });
  }

  /**
   * Remove a device profile attribute.
   * @param keyName The key-name of the attribute to be removed.
   * @param label The label of the attribute to be removed.
   */
  remove(keyName: any, label: string) {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Attribute",
        question: "Do you really want to delete '''" + label + "''' attribute?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.devicesService.removeDeviceAttribute(this.id!, keyName).subscribe(() => {
          this.utilityService.popupSuccess("Device attribute successfully deleted.");
          this.fetchAttributes();
        });
      }
    });

  }
}
