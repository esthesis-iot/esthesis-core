import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SecurityRoutingModule} from "./security-routing.module";
import {
  SecurityUsersListComponent
} from "./security-users/security-users-list/security-users-list.component";
import {
  SecurityUsersEditComponent
} from "./security-users/security-users-edit/security-users-edit.component";
import {
  SecurityGroupsEditComponent
} from "./security-groups/security-groups-edit/security-groups-edit.component";
import {
  SecurityGroupsListComponent
} from "./security-groups/security-groups-list/security-groups-list.component";
import {
  SecurityRolesListComponent
} from "./security-roles/security-roles-list/security-roles-list.component";
import {
  SecurityRolesEditComponent
} from "./security-roles/security-roles-edit/security-roles-edit.component";
import {
  SecurityPoliciesEditComponent
} from "./security-policies/security-policies-edit/security-policies-edit.component";
import {
  SecurityPoliciesListComponent
} from "./security-policies/security-policies-list/security-policies-list.component";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {ReactiveFormsModule} from "@angular/forms";
import {CdkTableModule} from "@angular/cdk/table";
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatInputModule} from "@angular/material/input";
import {ComponentsModule} from "../shared/components/components.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import { SecurityPoliciesEditorComponent } from './security-policies/security-policies-editor/security-policies-editor.component';
import {MatSelectModule} from "@angular/material/select";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import { SecurityPolicyTesterComponent } from './security-policies/security-policy-tester/security-policy-tester.component';

@NgModule({
  declarations: [

    SecurityUsersListComponent,
    SecurityUsersEditComponent,
    SecurityGroupsEditComponent,
    SecurityGroupsListComponent,
    SecurityRolesListComponent,
    SecurityRolesEditComponent,
    SecurityPoliciesEditComponent,
    SecurityPoliciesListComponent,
    SecurityPoliciesEditorComponent,
    SecurityPolicyTesterComponent
  ],
  imports: [
    CommonModule,
    SecurityRoutingModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    CdkTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatInputModule,
    ComponentsModule,
    MatTooltipModule,
    MatSelectModule,
    MatAutocompleteModule
  ]
})
export class SecurityModule {
}
