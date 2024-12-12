import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {LiveToggleComponent} from "./live-toggle/live-toggle.component";
import {BreadcrumbComponent} from "./breadcrumb/breadcrumb.component";
import {OkCancelModalComponent} from "./ok-cancel-modal/ok-cancel-modal.component";
import {BooleanCheckboxComponent} from "./boolean-checkbox/boolean-checkbox.component";
import {InputModalComponent} from "./input-modal/input-modal.component";
import {MatIconPickerComponent} from "./mat-icon-picker/mat-icon-picker.component";
import {FieldErrorComponent} from "./field-error/field-error.component";
import {TitlelisePipe} from "./titlelise/titlelise.pipe";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatIconModule} from "@angular/material/icon";
import {MatDialogModule} from "@angular/material/dialog";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CountdownComponent} from "ngx-countdown";
import {FramedMapComponent} from "./framed-map/framed-map.component";
import {AcDirective} from "./ac/ac.directive";

@NgModule({
  declarations: [
    LiveToggleComponent,
    BreadcrumbComponent,
    OkCancelModalComponent,
    BooleanCheckboxComponent,
    InputModalComponent,
    MatIconPickerComponent,
    FieldErrorComponent,
    TitlelisePipe,
    FramedMapComponent,
    AcDirective,
  ],
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatInputModule,
    MatTooltipModule,
    FontAwesomeModule,
    CountdownComponent
  ],
  exports: [
    BooleanCheckboxComponent,
    FieldErrorComponent,
    TitlelisePipe,
    BreadcrumbComponent,
    LiveToggleComponent,
    FramedMapComponent,
    AcDirective,
    MatIconPickerComponent
  ]
})
export class ComponentsModule {
}
