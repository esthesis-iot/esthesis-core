import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {CasRoutingModule} from "./cas-routing.module";
import {CasListComponent} from "./cas-list/cas-list.component";
import {CasEditComponent} from "./cas-edit/cas-edit.component";
import {CasImportComponent} from "./cas-import/cas-import.component";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatMenuModule} from "@angular/material/menu";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {MatTabsModule} from "@angular/material/tabs";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {DisplayModule} from "../shared/component/display/display.module";
import {DateSupportModule} from "../shared/module/date-support.module";
import {MatTooltipModule} from "@angular/material/tooltip";

@NgModule({
  declarations: [
    CasListComponent,
    CasEditComponent,
    CasImportComponent
  ],
  imports: [
    CommonModule,
    CasRoutingModule,
    FlexLayoutModule,
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
    DisplayModule,
    DateSupportModule,
    MatTooltipModule
  ]
})
export class CasModule {
}
