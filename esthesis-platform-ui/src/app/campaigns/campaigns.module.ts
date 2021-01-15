import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';

import {CampaignsRoutingModule} from './campaigns-routing.module';
import {CampaignsComponent} from './campaigns.component';
import {MatCardModule} from '@angular/material/card';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatMenuModule} from '@angular/material/menu';
import {MatSelectModule} from '@angular/material/select';
import {QFormsModule} from '@qlack/forms';
import {DisplayModule} from '../shared/component/display/display.module';
import {DateSupportModule} from '../shared/module/date-support.module';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {CampaignEditComponent} from './campaign/campaign-edit/campaign-edit.component';
import {LogPipeModule} from '../shared/module/log-pipe.module';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {DragDropModule} from '@angular/cdk/drag-drop';

@NgModule({
  schemas: [NO_ERRORS_SCHEMA],
  declarations: [CampaignsComponent, CampaignEditComponent],
  imports: [
    CommonModule,
    CampaignsRoutingModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    FlexLayoutModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    QFormsModule,
    DisplayModule,
    DateSupportModule,
    MatIconModule,
    MatDividerModule,
    LogPipeModule,
    MatAutocompleteModule,
    DragDropModule
  ]
})
export class CampaignsModule {
}
