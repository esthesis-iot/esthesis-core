import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {OkCancelModalComponent} from "./ok-cancel-modal/ok-cancel-modal.component";
import {MatButtonModule} from "@angular/material/button";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDialogModule} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";
import {BooleanCheckboxComponent} from "./boolean-checkbox/boolean-checkbox.component";
import {TextModalComponent} from "./text-modal/text-modal.component";
import {MatSelectModule} from "@angular/material/select";
import {FlexLayoutModule} from "@angular/flex-layout";
import {ReactiveFormsModule} from "@angular/forms";
import {InputModalComponent} from "./input-modal/input-modal.component";
import {MatInputModule} from "@angular/material/input";
import {MatIconPickerComponent} from "./mat-icon-picker/mat-icon-picker.component";
import {FieldErrorComponent} from "./field-error/field-error.component";
import {MatTooltipModule} from "@angular/material/tooltip";

@NgModule({
  declarations: [
    OkCancelModalComponent,
    BooleanCheckboxComponent,
    TextModalComponent,
    InputModalComponent,
    MatIconPickerComponent,
    FieldErrorComponent
  ],
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatSelectModule,
    FlexLayoutModule,
    ReactiveFormsModule,
    MatInputModule,
    MatTooltipModule
  ],
  exports: [
    BooleanCheckboxComponent,
    FieldErrorComponent
  ]
})
export class DisplayModule {
}
