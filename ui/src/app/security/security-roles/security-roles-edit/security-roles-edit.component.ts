import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {UtilityService} from "../../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../../shared/services/form-validation.service";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {SecurityRolesService} from "../../security-roles.service";
import {RoleDto} from "../../dto/role-dto";
import {SecurityPoliciesService} from "../../security-policies.service";
import {PolicyDto} from "../../dto/policy-dto";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import * as _ from "lodash";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-security-roles-edit",
  templateUrl: "./security-roles-edit.component.html"
})
export class SecurityRolesEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  allPolicies: PolicyDto[] = [];
  filteredPolicies: PolicyDto[] = [];
  policiesFilterCtrl = new FormControl();

  constructor(private fb: FormBuilder, private securityRolesService: SecurityRolesService,
    private route: ActivatedRoute, private qForms: QFormsService, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog,
    private qFormValidation: QFormValidationEEService,
    private securityPoliciesService: SecurityPoliciesService) {
    super(AppConstants.SECURITY.CATEGORY.ROLES, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [],
      description: [],
      policies: [[]]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.securityRolesService.findById(this.id).subscribe({
        next: (role) => {
          this.form.patchValue(role);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve this role.", err);
        }
      });
    }

    // Get all available policies.
    this.securityPoliciesService.find("sort=name,asc").subscribe({
      next: (policies) => {
        this.allPolicies = policies.content;
        this.filteredPolicies = this.allPolicies;
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve the list of policies.", err);
      }
    });

    // Watch policies autocomplete.
    this.policiesFilterCtrl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.filteredPolicies = _.filter(this.allPolicies, (policy) => {
            return policy.name.toLowerCase().indexOf(searchVal.toLowerCase()) !== -1 ||
            policy.description?.toLowerCase().indexOf(searchVal.toLowerCase()) !== -1;
          });
        } else {
          this.filteredPolicies = this.allPolicies;
        }
      }
    });
  }

  save() {
    this.securityRolesService.save(this.form.getRawValue() as RoleDto).subscribe({
      next: () => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("Role was successfully created.");
        } else {
          this.utilityService.popupSuccess("Role was successfully edited.");
        }
        this.router.navigate(["security/roles"]);
      }, error: (err) => {
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError("There was an error trying to save this role.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Role",
        question: "Do you really want to delete this role?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.securityRolesService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Role successfully deleted.");
            this.router.navigate(["security/roles"]);
          }
        });
      }
    });
  }

  addPolicy() {
    const policyId = this.policiesFilterCtrl.value;
    this.form.controls.policies.value.push(policyId);
    this.policiesFilterCtrl.patchValue("");
  }

  getPolicyName(policyId: string) {
    const policy = _.find(this.allPolicies, (p) => p.id === policyId);
    return policy?.name;
  }
  getPolicyDescription(policyId: string) {
    const policy = _.find(this.allPolicies, (p) => p.id === policyId);
    return policy?.description;
  }
  getPolicyRule(policyId: string) {
    const policy = _.find(this.allPolicies, (p) => p.id === policyId);
    return policy?.rule;
  }

  removePolicy(policyId: string) {
    const policies = this.form.controls.policies.value;
    const index = policies.indexOf(policyId);
    if (index >= 0) {
      policies.splice(index, 1);
    }
  }
}
