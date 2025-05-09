import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../../shared/services/form-validation.service";
import {RoleDto} from "../../dto/role-dto";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {SecurityGroupsService} from "../../security-groups.service";
import {GroupDto} from "../../dto/group-dto";
import {SecurityRolesService} from "../../security-roles.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import * as _ from "lodash-es";
import {
  SecurityPoliciesEditorComponent
} from "../../security-policies/security-policies-editor/security-policies-editor.component";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-security-groups-edit",
  templateUrl: "./security-groups-edit.component.html"
})
export class SecurityGroupsEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  allRoles: RoleDto[] = [];
  filteredRoles: RoleDto[] = [];
  rolesFilterCtrl = new FormControl();

  constructor(private readonly fb: FormBuilder,
    private readonly securityGroupsService: SecurityGroupsService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly utilityService: UtilityService, private readonly dialog: MatDialog,
    private readonly qFormValidation: QFormValidationEEService,
    private readonly securityRolesService: SecurityRolesService) {
    super(AppConstants.SECURITY.CATEGORY.GROUPS, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [],
      description: [],
      roles: [[]],
      policies: [[]]
    });
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.securityGroupsService.findById(this.id).subscribe({
        next: (group) => {
          this.form.patchValue(group);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve this group.", err);
        }
      });
    }

    // Get all available roles.
    this.securityRolesService.find("sort=name,asc").subscribe({
      next: (roles) => {
        this.allRoles = roles.content;
        this.filteredRoles = this.allRoles;
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve the list of roles.", err);
      }
    });

    // Watch roles autocomplete.
    this.rolesFilterCtrl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.filteredRoles = _.filter(this.allRoles, (role) => {
            return role.name.toLowerCase().indexOf(searchVal.toLowerCase()) !== -1 ||
              role.description?.toLowerCase().indexOf(searchVal.toLowerCase()) !== -1;
          });
        } else {
          this.filteredRoles = this.allRoles;
        }
      }
    });
  }

  save() {
    this.securityGroupsService.save(this.form.getRawValue() as GroupDto).subscribe({
      next: () => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("Group was successfully created.");
        } else {
          this.utilityService.popupSuccess("Group was successfully edited.");
        }
        this.router.navigate(["security/groups"]);
      }, error: (err) => {
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError("There was an error trying to save this group.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Group",
        question: "Do you really want to delete this group?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.securityGroupsService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Group successfully deleted.");
            this.router.navigate(["security/groups"]);
          }
        });
      }
    });
  }

  addRole() {
    const roleId = this.rolesFilterCtrl.value;
    this.form.controls["roles"].value.push(roleId);
    this.rolesFilterCtrl.patchValue("");
  }

  getRoleName(roleId: string) {
    const role = _.find(this.allRoles, (p) => p.id === roleId);
    return role?.name;
  }

  getRoleDescription(roleId: string) {
    const role = _.find(this.allRoles, (p) => p.id === roleId);
    return role?.description;
  }

  removeRole(roleId: string) {
    const roles = this.form.controls["roles"].value;
    const index = roles.indexOf(roleId);
    if (index >= 0) {
      roles.splice(index, 1);
    }
  }

  removePolicy(policy: any) {
    const policies = this.form.controls["policies"].value;
    const index = policies.indexOf(policy);
    if (index >= 0) {
      policies.splice(index, 1);
    }
  }

  policyEditor() {
    const editorDialogRef = this.dialog.open(SecurityPoliciesEditorComponent, {width: "40rem"});
    editorDialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.form.controls["policies"].value) {
          this.form.controls["policies"].value.push(result);
        } else {
          this.form.patchValue({
            policies: [result]
          });
        }
      }
    });
  }
}

