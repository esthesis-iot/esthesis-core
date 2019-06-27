import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommandComponent } from './command.component';
import {
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatIconModule, MatInputModule, MatSelectModule,
  MatStepperModule
} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';
import {FlexModule} from '@angular/flex-layout';

@NgModule({
  declarations: [CommandComponent],
  exports: [
    CommandComponent
  ],
  imports: [
    CommonModule,
    MatCardModule,
    MatStepperModule,
    ReactiveFormsModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FlexModule,
    MatSelectModule,
    MatButtonModule
  ]
})
export class CommandsModule { }
