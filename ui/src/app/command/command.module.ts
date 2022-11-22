import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {MatTooltipModule} from "@angular/material/tooltip";
import {ReactiveFormsModule} from "@angular/forms";
import {FlexLayoutModule} from "@angular/flex-layout";
import {QFormsModule} from "@qlack/forms";
import {MomentModule} from "ngx-moment";
import {CommandRoutingModule} from "./command-routing.module";
import {CommandComponent} from "./command.component";
import {CommandCreateComponent} from "./command-create/command-create.component";
import {CommandReplyComponent} from "./command-reply/command-reply.component";
import {MatStepperModule} from "@angular/material/stepper";
import {DateSupportModule} from "../shared/module/date-support.module";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {TruncateModule} from "@yellowspot/ng-truncate";
import {MatChipsModule} from "@angular/material/chips";
import {MatMenuModule} from "@angular/material/menu";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatCheckboxModule} from "@angular/material/checkbox";

@NgModule({
  declarations: [CommandComponent, CommandCreateComponent, CommandReplyComponent],
  imports: [
    CommonModule,
    CommandRoutingModule,
    FlexLayoutModule,
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
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class CommandModule {
}
