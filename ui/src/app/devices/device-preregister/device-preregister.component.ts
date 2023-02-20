import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DevicesService} from "../devices.service";
import {Router} from "@angular/router";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {DevicePreregisterCamComponent} from "./device-preregister-cam.component";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {QFormsService} from "@qlack/forms";
import {DeviceRegisterDto} from "../dto/device-register-dto";
import {QFormValidationEEService} from "../../shared/service/form-validation.service";
import {v4 as uuidv4} from "uuid";
import {Dialog} from "@angular/cdk/dialog";

@Component({
  selector: "app-device-preregister",
  templateUrl: "./device-preregister.component.html",
  styleUrls: []
})
export class DevicePreregisterComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, private qForms: QFormsService,
    private devicesService: DevicesService, private router: Router,
    private utilityService: UtilityService, private tagService: TagsService,
    private dialog: Dialog, private qFormValidation: QFormValidationEEService) {
    super();
  }

  ngOnInit() {
    // Set up the form.
    this.form = this.fb.group({
      ids: [null, [Validators.required]],
      tags: []
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
        this.form.controls.ids.patchValue(result);
      }
    });
  }
}
