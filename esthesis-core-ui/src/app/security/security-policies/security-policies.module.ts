import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SecurityPoliciesRoutingModule} from "./security-policies-routing.module";
import {
  SecurityPoliciesEditComponent
} from "./security-policies-edit/security-policies-edit.component";
import {
  SecurityPoliciesListComponent
} from "./security-policies-list/security-policies-list.component";
import {
  SecurityPoliciesEditorComponent
} from "./security-policies-editor/security-policies-editor.component";
import {
  SecurityPolicyTesterComponent
} from "./security-policy-tester/security-policy-tester.component";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {ReactiveFormsModule} from "@angular/forms";
import {CdkTableModule} from "@angular/cdk/table";
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatInputModule} from "@angular/material/input";
import {ComponentsModule} from "../../shared/components/components.module";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSelectModule} from "@angular/material/select";
import {MatAutocompleteModule} from "@angular/material/autocomplete";


@NgModule({
  declarations: [
    SecurityPoliciesEditComponent,
    SecurityPoliciesListComponent,
    SecurityPoliciesEditorComponent,
    SecurityPolicyTesterComponent
  ],
  imports: [
    CommonModule,
    SecurityPoliciesRoutingModule,
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
export class SecurityPoliciesModule { }
