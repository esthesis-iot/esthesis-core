import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {OkCancelModalComponent} from "./ok-cancel-modal/ok-cancel-modal.component";
import {MatIconModule} from "@angular/material/icon";
import {BooleanCheckboxComponent} from "./boolean-checkbox/boolean-checkbox.component";
import {TextModalComponent} from "./text-modal/text-modal.component";
import {ReactiveFormsModule} from "@angular/forms";
import {InputModalComponent} from "./input-modal/input-modal.component";
import {MatIconPickerComponent} from "./mat-icon-picker/mat-icon-picker.component";
import {FieldErrorComponent} from "./field-error/field-error.component";
import {TitlelisePipe} from "./titlelise/titlelise.pipe";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatDialogModule} from "@angular/material/dialog";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [
    OkCancelModalComponent,
    BooleanCheckboxComponent,
    TextModalComponent,
    InputModalComponent,
    MatIconPickerComponent,
    FieldErrorComponent,
    TitlelisePipe
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
    MatTooltipModule
  ],
  exports: [
    BooleanCheckboxComponent,
    FieldErrorComponent,
    TitlelisePipe
  ]
})
export class DisplayModule {
}
