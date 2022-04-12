import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OkCancelModalComponent} from './ok-cancel-modal/ok-cancel-modal.component';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDialogModule} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {BooleanCheckboxComponent} from './boolean-checkbox/boolean-checkbox.component';
import {TextModalComponent} from './text-modal/text-modal.component';
import {MatSelectModule} from '@angular/material/select';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
    declarations: [
        OkCancelModalComponent,
        BooleanCheckboxComponent,
        TextModalComponent,
    ],
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatCheckboxModule,
        MatIconModule,
        MatSelectModule,
        FlexLayoutModule,
        ReactiveFormsModule
    ],
    exports: [
        BooleanCheckboxComponent
    ]
})
export class DisplayModule {
}
