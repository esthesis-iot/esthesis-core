import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {ApplicationsListComponent} from "./applications-list/applications-list.component";
import {ApplicationEditComponent} from "./application-edit/application-edit.component";
import {
  ApplicationEditDescriptionComponent
} from "./application-edit/application-edit-description.component";
import {
  ApplicationEditPermissionsComponent
} from "./application-edit/application-edit-permissions.component";
import {ApplicationsRoutingModule} from "./applications-routing.module";
import {DisplayModule} from "../shared/component/display/display.module";
import {DateSupportModule} from "../shared/module/date-support.module";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [
    ApplicationsListComponent,
    ApplicationEditComponent,
    ApplicationEditDescriptionComponent,
    ApplicationEditPermissionsComponent,
  ],
  imports: [
    ApplicationsRoutingModule,
    CommonModule,
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
    DateSupportModule
  ]
})
export class ApplicationsModule {
}
