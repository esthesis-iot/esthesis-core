import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SecurityGroupsRoutingModule} from "./security-groups-routing.module";
import {SecurityGroupsEditComponent} from "./security-groups-edit/security-groups-edit.component";
import {SecurityGroupsListComponent} from "./security-groups-list/security-groups-list.component";
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
    SecurityGroupsEditComponent,
    SecurityGroupsListComponent,
  ],
  imports: [
    CommonModule,
    SecurityGroupsRoutingModule,
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
export class SecurityGroupsModule { }
