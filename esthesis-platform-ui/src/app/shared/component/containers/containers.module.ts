import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ContainersRoutingModule} from './containers-routing.module';
import {ContainerDeployComponent} from './container-deploy.component';
import {
  MatButtonModule,
  MatButtonToggleModule,
  MatCardModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatSelectModule,
  MatStepperModule
} from '@angular/material';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
  declarations: [
    ContainerDeployComponent,
  ],
  imports: [
    CommonModule,
    ContainersRoutingModule,
    MatStepperModule,
    MatCardModule,
    FlexLayoutModule,
    ReactiveFormsModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatMenuModule,
    MatButtonModule,
    MatSelectModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class ContainersModule {
}
