import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SecurityUsersRoutingModule} from "./security-users-routing.module";
import {SecurityUsersListComponent} from "./security-users-list/security-users-list.component";
import {SecurityUsersEditComponent} from "./security-users-edit/security-users-edit.component";
import {SecurityRoutingModule} from "../security-routing.module";
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
    SecurityUsersListComponent,
    SecurityUsersEditComponent,
  ],
  imports: [
    CommonModule,
    SecurityUsersRoutingModule,
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
export class SecurityUsersModule { }
