import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
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
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {FlexLayoutModule} from "@angular/flex-layout";
import {ApplicationsComponent} from "./applications.component";
import {ApplicationEditComponent} from "./application-edit/application-edit.component";
import {ApplicationEditDescriptionComponent} from "./application-edit/application-edit-description.component";
import {ApplicationEditPermissionsComponent} from "./application-edit/application-edit-permissions.component";
import {ApplicationsRoutingModule} from "./applications-routing.module";
import {DisplayModule} from "../shared/component/display/display.module";
import {DateSupportModule} from "../shared/module/date-support.module";

@NgModule({
  declarations: [
    ApplicationsComponent,
    ApplicationEditComponent,
    ApplicationEditDescriptionComponent,
    ApplicationEditPermissionsComponent,
  ],
  imports: [
    ApplicationsRoutingModule,
    CommonModule,
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
    DateSupportModule
  ]
})
export class ApplicationsModule {
}
