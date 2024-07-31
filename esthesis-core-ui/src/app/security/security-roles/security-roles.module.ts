import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SecurityRolesRoutingModule} from "./security-roles-routing.module";
import {SecurityRolesListComponent} from "./security-roles-list/security-roles-list.component";
import {SecurityRolesEditComponent} from "./security-roles-edit/security-roles-edit.component";
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
    SecurityRolesListComponent,
    SecurityRolesEditComponent
  ],
  imports: [
    CommonModule,
    SecurityRolesRoutingModule,
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
export class SecurityRolesModule { }
