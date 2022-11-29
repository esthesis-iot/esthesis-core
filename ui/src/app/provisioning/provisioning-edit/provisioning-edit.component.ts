import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {QFormsService} from "@qlack/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient, HttpEventType} from "@angular/common/http";
import {BaseComponent} from "../../shared/component/base-component";
import {TagDto} from "../../dto/tag-dto";
import {TagService} from "../../tags/tag.service";
import {ProvisioningService} from "../provisioning.service";
import {UtilityService} from "../../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import * as _ from "lodash";

@Component({
  selector: "app-provisioning-edit",
  templateUrl: "./provisioning-edit.component.html",
  styleUrls: []
})
export class ProvisioningEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagService: TagService,
    private provisioningService: ProvisioningService, private route: ActivatedRoute,
    private router: Router, private http: HttpClient, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id")!;

    // // Setup the form.
    this.form = this.fb.group({
      id: [],
      name: ["", [Validators.required, Validators.maxLength(256)]],
      description: ["", [Validators.maxLength(2048)]],
      version: ["", [Validators.required]],
      prerequisiteVersion: ["", []],
      tags: [[]],
      attributes: [""],
      type: [{value: "", disabled: this.id !== this.appConstants.NEW_RECORD_ID}, [Validators.required]],
      available: [true, [Validators.required]],

      // ESTHESIS type
      fileName: [""],
      file: [],

      // WEB type
      webUrl: [""],
      webUsername: [""],
      webPassword: [""],

      // FTP type
      FTP_HOST: [],
      FTP_USERNAME: [],
      FTP_PASSWORD: [],
      FTP_PORT: [],
      FTP_PATH: [],
      FTP_PASSIVE: [],

      // S3
      s3Url: [""],

      // Minio
      minioUrl: [""],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      this.provisioningService.findById(this.id).subscribe({
        next: (next: any) => {
          // Patch common fields.
          this.form.patchValue(next);

          // Patch type-specific fields.
          switch (next.type) {
            case this.appConstants.PROVISIONING.TYPE.FTP:

              break;
          }
        }, error: (error: any) => {
          this.utilityService.popupErrorWithTraceId("Could not fetch provisioning package.", error);
        }
      });
    }

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe({
      next: (next: any) => {
        this.availableTags = next.content;
      }, error: (err: any) => {
        this.utilityService.popupErrorWithTraceId("There was a problem fetching available tags.", err);
      }
    });
  }

  save() {
    // Create a custom form to submit to the backend as the front-end form can not match what the
    // backend is expecting to receive.
    // Add common fields.
    const patchedForm = new FormGroup({});
    if (this.form.controls.id.value) {
      patchedForm.addControl("id", this.form.controls.id);
    }
    patchedForm.addControl("name", this.form.controls.name);
    patchedForm.addControl("description", this.form.controls.description);
    patchedForm.addControl("version", this.form.controls.version);
    patchedForm.addControl("prerequisiteVersion", this.form.controls.prerequisiteVersion);
    patchedForm.addControl("tags", this.form.controls.tags);
    patchedForm.addControl("attributes", this.form.controls.attributes);
    patchedForm.addControl("type", this.form.controls.type);
    patchedForm.addControl("available", this.form.controls.available);
    patchedForm.addControl("file", this.form.controls.file);
    patchedForm.addControl("fileName", this.form.controls.fileName);

    // Add type-specific fields.
    let typeSpecificConfiguration = "";
    _.forEach(this.form.controls, (value, key) => {
      if (key.startsWith(this.form.controls.type.value) && value.value) {
        typeSpecificConfiguration += key + "=" + value.value + ",";
      }
    });
    if (typeSpecificConfiguration.endsWith(",")) {
      typeSpecificConfiguration = typeSpecificConfiguration.substring(0, typeSpecificConfiguration.length - 1);
    }
    patchedForm.addControl("typeSpecificConfiguration", this.fb.control(typeSpecificConfiguration));

    // Save the patched form.
    this.provisioningService.upload(patchedForm).subscribe({
      next: (next: any) => {
        if (next.type === HttpEventType.Response) {
          if (next.status === 200) {
            this.utilityService.popupSuccess("Provisioning package successfully saved.");
            this.router.navigate(["provisioning"]);
          } else {
            this.utilityService.popupError("There was a problem uploading the provisioning package.");
          }
        }
      }, error: (err: any) => {
        this.utilityService.popupErrorWithTraceId("There was a problem uploading the provisioning package.", err);
      }
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Provisioning package",
        question: "Do you really want to delete this Provisioning package?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.provisioningService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess("Provisioning package successfully deleted.");
          this.router.navigate(["provisioning"]);
        });
      }
    });
  }

  selectFile(event: any) {
    this.form.controls.file.patchValue(event.target.files[0]);
    this.form.controls.fileName.patchValue(event.target.files[0].name);
  }

  download() {
    // this.provisioningService.download(this.id!);
  }

  recache() {
    this.provisioningService.recache(this.id).subscribe(onNext => {
      this.utilityService.popupSuccess("Provisioning package is being recached.");
      // this.router.navigate(["provisioning"]);
    });
  }
}
