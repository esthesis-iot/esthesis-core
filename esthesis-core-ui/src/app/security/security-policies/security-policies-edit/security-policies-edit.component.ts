import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../../shared/services/form-validation.service";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {SecurityPoliciesService} from "../../security-policies.service";
import {PolicyDto} from "../../dto/policy-dto";
import {
  SecurityPoliciesEditorComponent
} from "../security-policies-editor/security-policies-editor.component";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-security-policies-edit",
  templateUrl: "./security-policies-edit.component.html",
  styleUrls: []
})
export class SecurityPoliciesEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;

  constructor(private readonly fb: FormBuilder,
    private readonly securityPoliciesService: SecurityPoliciesService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly utilityService: UtilityService, private readonly dialog: MatDialog,
    private readonly qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.POLICIES, route.snapshot.paramMap.get("id"));
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
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

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
