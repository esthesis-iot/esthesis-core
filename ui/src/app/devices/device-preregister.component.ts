import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DevicesService} from "./devices.service";
import {Router} from "@angular/router";
import {TagDto} from "../dto/tag-dto";
import {TagService} from "../tags/tag.service";
import {DevicePreregisterCamComponent} from "./device-preregister-cam.component";
import {MatDialog} from "@angular/material/dialog";
import {BaseComponent} from "../shared/component/base-component";
import {UtilityService} from "../shared/service/utility.service";
import {QFormsService} from "@qlack/forms";
import {DeviceRegisterDto} from "../dto/device-register-dto";
import {QFormValidationEEService} from "../shared/service/form-validation.service";
import {v4 as uuidv4} from "uuid";

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
    private utilityService: UtilityService, private tagService: TagService,
    private dialog: MatDialog, private qFormValidation: QFormValidationEEService) {
    super();
  }

  ngOnInit() {
    // Setup the form.
    this.form = this.fb.group({
      ids: ["", [Validators.required]],
      tags: [[]]
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe({
      next: (next) => {
        this.availableTags = next.content;
      },
      error: (err) => {
        this.utilityService.popupError("There was an error while getting available tags, please try again later.");
      }
    });
  }

  save() {
    this.devicesService.preregister(
      this.qForms.cleanupData(this.form.getRawValue()) as DeviceRegisterDto).subscribe({
      next: (next) => {
        this.utilityService.popupSuccess("Devices successfully preregistered.");
        this.router.navigate(["devices"]);
      },
      error: (err) => {
        if (err.status == 400) {
          let validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.validateForm(this.form, validationErrors.violations);
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
        wsId: wsId
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.form.controls["ids"].patchValue(result);
      }
    });
  }
}
