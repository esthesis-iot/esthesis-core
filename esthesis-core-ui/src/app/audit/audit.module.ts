import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AuditRoutingModule} from "./audit-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {AuditListComponent} from "./audit-list/audit-list.component";
import {QFormsModule} from "@qlack/forms";
import {AuditViewComponent} from "./audit-view/audit-view.component";
import {NgxJsonViewerModule} from "ngx-json-viewer";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {DateSupportModule} from "../shared/modules/date-support.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";

@NgModule({
  declarations: [
    AuditListComponent,
    AuditViewComponent
  ],
  imports: [
    CommonModule,
    AuditRoutingModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    DateSupportModule,
    NgxJsonViewerModule,
    FontAwesomeModule,
    CdkTableModule
  ]
})
export class AuditModule {
}
