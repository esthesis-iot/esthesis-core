import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {ApplicationsListComponent} from "./applications-list/applications-list.component";
import {ApplicationEditComponent} from "./application-edit/application-edit.component";
import {ApplicationsRoutingModule} from "./applications-routing.module";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatButtonModule} from "@angular/material/button";
import {ComponentsModule} from "../shared/components/components.module";
import {DateSupportModule} from "../shared/modules/date-support.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";
import {MatTooltipModule} from "@angular/material/tooltip";

@NgModule({
  declarations: [
    ApplicationsListComponent,
    ApplicationEditComponent,
    ApplicationEditComponent,
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
    ComponentsModule,
    DateSupportModule,
    FontAwesomeModule,
    CdkTableModule,
    MatTooltipModule
  ]
})
export class ApplicationsModule {
}
