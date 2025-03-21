import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {CasRoutingModule} from "./cas-routing.module";
import {CasListComponent} from "./cas-list/cas-list.component";
import {CasEditComponent} from "./cas-edit/cas-edit.component";
import {CasImportComponent} from "./cas-import/cas-import.component";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatButtonModule} from "@angular/material/button";
import {MatTooltipModule} from "@angular/material/tooltip";
import {ComponentsModule} from "../shared/components/components.module";
import {DateSupportModule} from "../shared/modules/date-support.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";

@NgModule({
  declarations: [
    CasListComponent,
    CasEditComponent,
    CasImportComponent
  ],
  imports: [
    CommonModule,
    CasRoutingModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    MatTabsModule,
    ComponentsModule,
    DateSupportModule,
    MatTooltipModule,
    FontAwesomeModule,
    CdkTableModule
  ]
})
export class CasModule {
}
