import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../../shared/services/form-validation.service";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {SecurityService} from "../../security.service";
import {UserDto} from "../../dto/user-dto";
import {GroupDto} from "../../dto/group-dto";
import {SecurityGroupsService} from "../../security-groups.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import * as _ from "lodash-es";
import {
  SecurityPoliciesEditorComponent
} from "../../security-policies/security-policies-editor/security-policies-editor.component";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";
import {
  SecurityPolicyTesterComponent
} from "../../security-policies/security-policy-tester/security-policy-tester.component";

@Component({
  selector: "app-security-users-edit",
  templateUrl: "./security-users-edit.component.html",
  styleUrls: []
})
export class SecurityUsersEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  allGroups: GroupDto[] = [];
  filteredGroups: GroupDto[] = [];
  groupsFilterCtrl = new FormControl();

  constructor(private readonly fb: FormBuilder,
    private readonly securityUsersService: SecurityService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly utilityService: UtilityService, private readonly dialog: MatDialog,
    private readonly qFormValidation: QFormValidationEEService,
    private readonly securityGroupsService: SecurityGroupsService) {
    super(AppConstants.SECURITY.CATEGORY.USERS, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      username: [null, [Validators.required]],
      firstName: [],
      lastName: [],
      email: [],
      description: [null, [Validators.maxLength(2048)]],
      groups: [[]],
      policies: [[]]
    });
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.securityUsersService.findById(this.id).subscribe({
        next: (user) => {
          this.form.patchValue(user);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve this user.", err);
        }
      });
    }

    // Get all available groups.
    this.securityGroupsService.find("sort=name,asc").subscribe({
      next: (groups) => {
        this.allGroups = groups.content;
        this.filteredGroups = this.allGroups;
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve the list of groups.", err);
      }
    });

    // Watch groups autocomplete.
    this.groupsFilterCtrl.valueChanges.pipe(debounceTime(500), distinctUntilChanged()).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.filteredGroups = _.filter(this.allGroups, (group) => {
            return group.name.toLowerCase().indexOf(searchVal.toLowerCase()) !== -1 ||
              group.description?.toLowerCase().indexOf(searchVal.toLowerCase()) !== -1;
          });
        } else {
          this.filteredGroups = this.allGroups;
        }
      }
    });
  }

  save() {
    this.securityUsersService.save(this.form.getRawValue() as UserDto).subscribe({
      next: () => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("User was successfully created.");
        } else {
          this.utilityService.popupSuccess("User was successfully edited.");
        }
        this.router.navigate(["security/users"]);
      }, error: (err) => {
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError("There was an error trying to save this user.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete User",
        question: "Do you really want to delete this user?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.securityUsersService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("User successfully deleted.");
            this.router.navigate(["security/users"]);
          }
        });
      }
    });
  }

  addGroup() {
    // TODO not sure why this if-block is needed, since 'groups' is initialized as an empty array
    // TODO in the form definition. All other similar forms in security don't need this.
    if (!this.form.controls["groups"].value) {
      this.form.controls["groups"].patchValue([]);
    }
    const groupId = this.groupsFilterCtrl.value;
    this.form.controls["groups"].value.push(groupId);
    this.groupsFilterCtrl.patchValue("");
  }

  getGroupName(groupId: string) {
    const group = _.find(this.allGroups, (p) => p.id === groupId);
    return group?.name;
  }

  getGroupDescription(groupId: string) {
    const group = _.find(this.allGroups, (p) => p.id === groupId);
    return group?.description;
  }

  removeGroup(groupId: string) {
    const groups = this.form.controls["groups"].value;
    const index = groups.indexOf(groupId);
    if (index >= 0) {
      groups.splice(index, 1);
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

  policyTester() {
    this.dialog.open(SecurityPolicyTesterComponent, {
      width: "40rem",
    });
  }
}
