import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';

import {InfrastructureRoutingModule} from './infrastructure-routing.module';
import {InfrastructureComponent} from './infrastructure.component';
import {InfrastructureOverviewComponent} from './infrastructure-overview/infrastructure-overview.component';
import {InfrastructureMqttComponent} from './infrastructure-mqtt/infrastructure-mqtt.component';
import {InfrastructureMqttEditComponent} from './infrastructure-mqtt/infrastructure-mqtt-edit.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatMenuModule} from '@angular/material/menu';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {MatTabsModule} from '@angular/material/tabs';
import {QFormsModule} from '@qlack/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {DisplayModule} from '../shared/component/display/display.module';
import {InfrastructureNiFiComponent} from './infrastructure-nifi/infrastructure-nifi.component';
import {InfrastructureNiFiEditComponent} from './infrastructure-nifi/infrastructure-nifi-edit.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@NgModule({
  declarations: [
    InfrastructureComponent,
    InfrastructureOverviewComponent,
    InfrastructureMqttComponent,
    InfrastructureMqttEditComponent,
    InfrastructureNiFiComponent,
    InfrastructureNiFiEditComponent,
  ],
  imports: [
    CommonModule,
    InfrastructureRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    MatTabsModule,
    DisplayModule,
    MatProgressSpinnerModule,
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class InfrastructureModule {
}
