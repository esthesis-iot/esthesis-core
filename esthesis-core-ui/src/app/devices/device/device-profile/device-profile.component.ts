import {Component, OnInit} from "@angular/core";
import {FormArray, FormBuilder, FormGroup} from "@angular/forms";
import {DevicesService} from "../../devices.service";
import {ActivatedRoute} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../../shared/services/utility.service";
import {DeviceAttributeDto} from "../../dto/device-attribute-dto";
import {DevicePageFieldDataDto} from "../../dto/device-page-field-data-dto";
import {InputModalComponent} from "../../../shared/components/input-modal/input-modal.component";
import slugify from "slugify";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-device-profile",
  templateUrl: "./device-profile.component.html"
})
export class DeviceProfileComponent extends SecurityBaseComponent implements OnInit {
  id!: string | null;
  // The device profile form.
  deviceProfileForm!: FormGroup;

  // The device profile fields.
  deviceProfileFields?: DevicePageFieldDataDto[];

  constructor(private readonly fb: FormBuilder,
    private readonly devicesService: DevicesService, private readonly route: ActivatedRoute,
    private readonly utilityService: UtilityService, private readonly dialog: MatDialog) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE, route.snapshot.paramMap.get("id"));
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the Device profile form.
    this.deviceProfileForm = this.fb.group({
      attributes: this.fb.array<DeviceAttributeDto>([])
    });

    // Get Device profile.
    this.getProfile();

    this.isFormDisabled().subscribe(disabled => disabled && this.deviceProfileForm.disable());
  }

  public getAttributeFormGroup() {
    // @ts-ignore
    return this.deviceProfileForm.get("attributes").controls;
  }

  /**
   * Save the device profile.
   */
  save() {
    this.devicesService.saveProfile(this.id!, this.deviceProfileForm.getRawValue()).subscribe({
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
  addAttribute() {
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
        // @ts-ignore
        this.deviceProfileForm.controls.attributes.push(
          this.generateAttributeFormGroup({
            attributeName: slugify(result).toLowerCase(),
            attributeValue: "",
            attributeType: this.appConstants.DEVICE.ATTRIBUTE.TYPE.UNKNOWN
          })
        );
      }
    });
  }

  deleteAttribute(i: number) {
    (this.deviceProfileForm.controls['attributes'] as FormArray).removeAt(i);
  }

  private generateAttributeFormGroup(attribute: DeviceAttributeDto): FormGroup {
    return this.fb.group({
      deviceId: attribute.deviceId,
      attributeName: attribute.attributeName,
      attributeValue: attribute.attributeValue,
      attributeType: attribute.attributeType
    });
  }

  private getProfile() {
    this.devicesService.getProfile(this.id!).subscribe({
      next: (profile) => {
        this.deviceProfileForm.controls['attributes'] = this.fb.array<DeviceAttributeDto>([]);
        profile.attributes.forEach((attribute) => {
          // @ts-ignore
          this.deviceProfileForm.controls.attributes.push(this.generateAttributeFormGroup(attribute));
        });
        this.deviceProfileFields = profile.fields;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch device attributes.", error);
      }
    });
  }
}
