import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OkCancelModalComponent} from './ok-cancel-modal/ok-cancel-modal.component';
import {
  MatButtonModule,
  MatCheckboxModule,
  MatDialogModule,
  MatIconModule
} from '@angular/material';
import {BooleanCheckboxComponent} from './boolean-checkbox/boolean-checkbox.component';
import {TextModalComponent} from './text-modal/text-modal.component';

@NgModule({
  declarations: [
    OkCancelModalComponent,
    BooleanCheckboxComponent,
    TextModalComponent
  ],
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule
  ],
  entryComponents: [OkCancelModalComponent],
  exports: [
    BooleanCheckboxComponent
  ]
})
export class DisplayModule {
}
