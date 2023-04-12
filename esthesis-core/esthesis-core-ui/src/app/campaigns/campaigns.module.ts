import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {CampaignsRoutingModule} from "./campaigns-routing.module";
import {CampaignsComponent} from "./campaigns-list/campaigns.component";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {MatIconModule} from "@angular/material/icon";
import {MatDividerModule} from "@angular/material/divider";
import {CampaignEditComponent} from "./campaign-edit/campaign-edit.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {MatSortModule} from "@angular/material/sort";
import {NgCircleProgressModule} from "ng-circle-progress";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatTableModule} from "@angular/material/table";
import {MatSelectModule} from "@angular/material/select";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";
import {MatTooltipModule} from "@angular/material/tooltip";
import {CountdownComponent} from "ngx-countdown";
import {ComponentsModule} from "../shared/components/components.module";
import {DateSupportModule} from "../shared/modules/date-support.module";

@NgModule({
  schemas: [NO_ERRORS_SCHEMA],
  declarations: [CampaignsComponent, CampaignEditComponent],
  imports: [
    CommonModule,
    CampaignsRoutingModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    QFormsModule,
    MatSortModule,
    ComponentsModule,
    DateSupportModule,
    MatIconModule,
    MatDividerModule,
    MatAutocompleteModule,
    DragDropModule,
    MatProgressBarModule,
    NgCircleProgressModule.forRoot({}),
    MatExpansionModule,
    FontAwesomeModule,
    CdkTableModule,
    MatTooltipModule,
    CountdownComponent
  ]
})
export class CampaignsModule {
}
