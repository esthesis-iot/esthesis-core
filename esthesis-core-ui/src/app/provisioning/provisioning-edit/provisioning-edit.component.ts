import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpEventType} from "@angular/common/http";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {ProvisioningService} from "../provisioning.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {ProvisioningDto} from "../dto/provisioning-dto";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";

@Component({
  selector: "app-provisioning-edit",
  templateUrl: "./provisioning-edit.component.html"
})
export class ProvisioningEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  availableTags: TagDto[] | undefined;
  provisioningPackage?: ProvisioningDto;
  baseVersions?: ProvisioningDto[];

  constructor(private fb: FormBuilder, private dialog: MatDialog, private tagService: TagsService,
    private provisioningService: ProvisioningService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService,
    private qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.PROVISIONING, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id")!;

    // // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(256)]],
      description: [null, [Validators.maxLength(2048)]],
      version: [null, [Validators.required]],
      prerequisiteVersion: [null, []],
      tags: [[]],
      attributes: [],
      type: [{value: null, disabled: this.id !== this.appConstants.NEW_RECORD_ID}, [Validators.required]],
      available: [true, [Validators.required]],
      sha256: [],

      // INTERNAL type
      fileName: [],
      file: [],

      // EXTERNAL type
      WEB_URL: [{value: null, disabled: this.id !== this.appConstants.NEW_RECORD_ID}]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      this.provisioningService.findById(this.id).subscribe({
        next: (next: ProvisioningDto) => {
          // Set a reference to the loaded provisioning package.
          this.provisioningPackage = next;

          // Patch common fields.
          this.form.patchValue(next);

          // Patch type-specific fields.
          if (next.typeSpecificConfiguration) {
            next.typeSpecificConfiguration.split(",").forEach((value: string) => {
              const keyValuePair = value.split("=");
              this.form.controls[keyValuePair[0]].setValue(keyValuePair[1]);
            });
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

    // Get available base versions.
    this.findBaseVersions();

    // If tags change, update base versions.
    this.form.controls['tags'].valueChanges.pipe(
      debounceTime(100), distinctUntilChanged()
    ).subscribe(() => {
      this.findBaseVersions();
    });
  }

  save() {
    // Create a custom form to submit to the backend combining type-specific configuration into a
    // single field.
    // Add common fields.
    const patchedForm = new FormGroup({});
    if (this.form.controls['id'].value) {
      patchedForm.addControl("id", this.form.controls['id']);
    }
    patchedForm.addControl("name", this.form.controls['name']);
    patchedForm.addControl("description", this.form.controls['description']);
    patchedForm.addControl("version", this.form.controls['version']);
    patchedForm.addControl("prerequisiteVersion", this.form.controls['prerequisiteVersion']);
    patchedForm.addControl("tags", this.form.controls['tags']);
    patchedForm.addControl("attributes", this.form.controls['attributes']);
    patchedForm.addControl("type", this.form.controls['type']);
    patchedForm.addControl("available", this.form.controls['available']);
    patchedForm.addControl("file", this.form.controls['file']);
    patchedForm.addControl("fileName", this.form.controls['fileName']);
    patchedForm.addControl("sha256", this.form.controls['sha256']);

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
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupErrorWithTraceId("There was a problem uploading the provisioning package.", err);
        }
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
        this.provisioningService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Provisioning package successfully deleted.");
            this.router.navigate(["provisioning"]);
          }
        });
      }
    });
  }

  selectFile(event: any) {
    this.form.controls['file'].patchValue(event.target.files[0]);
    this.form.controls['fileName'].patchValue(event.target.files[0].name);
  }

  download() {
    this.provisioningService.download(this.id);
  }

  /**
   * Finds other candidate versions to be a prerequisite for this version. Candidate versions are
   * filtered to match at least one of the tags of this version.
   */
  findBaseVersions() {
    const tags = this.form.controls['tags'].value.join(",");
    this.provisioningService.findBaseVersions(tags).subscribe({
      next: (next: any) => {
        this.baseVersions =
          next.filter((value: any) => value['version'] !== this.form.controls['version'].value);
      }, error: (err: any) => {
        this.utilityService.popupErrorWithTraceId("There was a problem fetching available base versions.", err);
      }
    });
  }
}
