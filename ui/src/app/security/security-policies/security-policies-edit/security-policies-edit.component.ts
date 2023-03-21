import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../../shared/components/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SecurityService} from "../../security.service";
import {ActivatedRoute, Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {UtilityService} from "../../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../../shared/services/form-validation.service";
import {UserDto} from "../../dto/user-dto";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {SecurityPoliciesService} from "../../security-policies.service";
import {PolicyDto} from "../../dto/policy-dto";
import {DevicesComponent} from "../../../devices/devices-list/devices.component";
import {MatTableDataSource} from "@angular/material/table";
import {KeystoreEntryDto} from "../../../keystores/dto/keystore-entry-dto";
import {
  SecurityPoliciesEditorComponent
} from "../security-policies-editor/security-policies-editor.component";

@Component({
  selector: "app-security-policies-edit",
  templateUrl: "./security-policies-edit.component.html",
  styleUrls: []
})
export class SecurityPoliciesEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;

  constructor(private fb: FormBuilder, private securityPoliciesService: SecurityPoliciesService,
    private route: ActivatedRoute, private qForms: QFormsService, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog,
    private qFormValidation: QFormValidationEEService) {
    super();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [null, [Validators.required]],
      description: [null, [Validators.maxLength(2048)]],
      rule: [null, [Validators.required]]
    });
    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.securityPoliciesService.findById(this.id).subscribe({
        next: (user) => {
          this.form.patchValue(user);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve this policy.", err);
        }
      });
    }
  }

  save() {
    this.securityPoliciesService.save(this.form.getRawValue() as PolicyDto).subscribe({
      next: () => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("Policy was successfully created.");
        } else {
          this.utilityService.popupSuccess("Policy was successfully edited.");
        }
        this.router.navigate(["security/policies"]);
      }, error: (err) => {
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError("There was an error trying to save this policy.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Policy",
        question: "Do you really want to delete this policy?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.securityPoliciesService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Policy successfully deleted.");
            this.router.navigate(["security/policies"]);
          }
        });
      }
    });
  }

  policyEditor() {
    const editorDialogRef = this.dialog.open(SecurityPoliciesEditorComponent, {
      width: "40rem",
    });
    editorDialogRef.componentInstance.existingErn = this.form.get("rule")!.value;
    editorDialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.form.patchValue({
          rule: result
        });
      }
    });
  }
}
