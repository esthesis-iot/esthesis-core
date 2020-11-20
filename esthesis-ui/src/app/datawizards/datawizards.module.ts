import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DatawizardsRoutingModule } from './datawizards-routing.module';
import { DatawizardsComponent } from './datawizards.component';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {MatRadioModule} from '@angular/material/radio';
import { DatawizardStandardComponent } from './datawizard-standard/datawizard-standard.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@NgModule({
  declarations: [DatawizardsComponent, DatawizardStandardComponent],
  imports: [
    CommonModule,
    DatawizardsRoutingModule,
    MatCardModule,
    MatButtonModule,
    FlexLayoutModule,
    MatFormFieldModule,
    MatInputModule,
    QFormsModule,
    ReactiveFormsModule,
    MatRadioModule,
    MatProgressBarModule,
    MatProgressSpinnerModule
  ]
})
export class DatawizardsModule { }
