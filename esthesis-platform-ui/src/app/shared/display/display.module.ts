import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OkCancelModalComponent} from './ok-cancel-modal/ok-cancel-modal.component';
import {MatButtonModule, MatDialogModule} from '@angular/material';

@NgModule({
  declarations: [
    OkCancelModalComponent
  ],
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule
  ],
  entryComponents: [OkCancelModalComponent],
})
export class DisplayModule {
}
