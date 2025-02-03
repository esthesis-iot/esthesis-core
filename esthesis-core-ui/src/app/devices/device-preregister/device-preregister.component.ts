import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DevicesService} from "../devices.service";
import {Router} from "@angular/router";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {DevicePreregisterCamComponent} from "./device-preregister-cam.component";
import {DeviceRegisterDto} from "../dto/device-register-dto";
import {v4 as uuidv4} from "uuid";
import {Dialog} from "@angular/cdk/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-device-preregister",
  templateUrl: "./device-preregister.component.html",
  styleUrls: []
})
export class DevicePreregisterComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  availableTags: TagDto[] | undefined;

  constructor(private readonly fb: FormBuilder,
    private readonly devicesService: DevicesService, private readonly router: Router,
    private readonly utilityService: UtilityService, private readonly tagService: TagsService,
    private readonly dialog: Dialog, private readonly qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE);
  }

  ngOnInit() {
    // Set up the form.
    this.form = this.fb.group({
      hardwareId: [null, [Validators.required]],
      tags: [],
      type: this.appConstants.DEVICE.TYPE.CORE
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe({
      next: (next) => {
        this.availableTags = next.content;
      },
      error: () => {
        this.utilityService.popupError("There was an error while getting available tags, please try again later.");
      }
    });
  }

  save() {
    this.devicesService.preregister(this.form.getRawValue() as DeviceRegisterDto).subscribe({
      next: () => {
        this.utilityService.popupSuccess("Devices successfully preregistered.");
        this.router.navigate(["devices"]);
      },
      error: (err) => {
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError("There was an error while preregistering devices, please try again later.");
        }
      }
    });
  }

  useCam() {
    const wsId = uuidv4();
    const dialogRef = this.dialog.open(DevicePreregisterCamComponent, {
      height: "70%",
      width: "80%",
      data: {
        wsId
      }
    });
    dialogRef.closed.subscribe(result => {
      if (result) {
        this.form.controls['hardwareId'].patchValue(result);
      }
    });
  }
}
