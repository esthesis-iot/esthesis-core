import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {MomentModule} from "ngx-moment";
import {CommandsRoutingModule} from "./commands-routing.module";
import {CommandsListComponent} from "./commands-list/commands-list.component";
import {CommandCreateComponent} from "./command-create/command-create.component";
import {CommandReplyComponent} from "./command-reply/command-reply.component";
import {MatStepperModule} from "@angular/material/stepper";
import {DateSupportModule} from "../shared/module/date-support.module";
import {TruncateModule} from "@yellowspot/ng-truncate";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatButtonModule} from "@angular/material/button";
import {MatMenuModule} from "@angular/material/menu";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatChipsModule} from "@angular/material/chips";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";

@NgModule({
  declarations: [CommandsListComponent, CommandCreateComponent, CommandReplyComponent],
  imports: [
    CommonModule,
    CommandsRoutingModule,
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
    MomentModule,
    MatTooltipModule,
    MatStepperModule,
    DateSupportModule,
    MatProgressSpinnerModule,
    TruncateModule,
    MatChipsModule,
    MatMenuModule,
    MatAutocompleteModule,
    MatCheckboxModule,
    FontAwesomeModule,
    CdkTableModule,
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class CommandsModule {
}
