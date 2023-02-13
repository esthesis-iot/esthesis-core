import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {DashboardRoutingModule} from "./dashboard-routing.module";
import {DashboardViewComponent} from "./dashboard-view/dashboard-view.component";
import {MAT_DATE_FORMATS} from "@angular/material/core";
import {MatIconModule} from "@angular/material/icon";
import {GridsterModule} from "angular-gridster2";
import {ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatTableModule} from "@angular/material/table";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatInputModule} from "@angular/material/input";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSliderModule} from "@angular/material/slider";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";

// @ts-ignore
@NgModule({
  declarations: [
    DashboardViewComponent,
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    GridsterModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatAutocompleteModule,
    MatSliderModule,
    MatCheckboxModule,
    FontAwesomeModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
  providers: [
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: "LL",
        },
        display: {
          dateInput: "YYYY-MM-DD",
          monthYearLabel: "MMM YYYY",
          dateA11yLabel: "LL",
          monthYearA11yLabel: "MMMM YYYY",
        },
      }
    }
  ]
})
export class DashboardModule {
}
