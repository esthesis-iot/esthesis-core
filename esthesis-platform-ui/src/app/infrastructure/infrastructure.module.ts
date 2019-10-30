import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';

import {InfrastructureRoutingModule} from './infrastructure-routing.module';
import {InfrastructureComponent} from './infrastructure.component';
import {InfrastructureOverviewComponent} from './infrastructure-overview/infrastructure-overview.component';
import {InfrastructureMqttComponent} from './infrastructure-mqtt/infrastructure-mqtt.component';
import {InfrastructureMqttEditComponent} from './infrastructure-mqtt/infrastructure-mqtt-edit.component';
import {InfrastructureVirtualizationComponent} from './infrastructure-virtualization/infrastructure-virtualization.component';
import {InfrastructureVirtualizationEditComponent} from './infrastructure-virtualization/infrastructure-virtualization-edit.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {InfrastructureZookeeperComponent} from './infrastructure-zookeeper/infrastructure-zookeeper.component';
import {InfrastructureZookeeperEditComponent} from './infrastructure-zookeeper/infrastructure-zookeeper-edit.component';
import {DisplayModule} from '../shared/component/display/display.module';
import {ContainersModule} from '../shared/component/containers/containers.module';
import { InfrastructureNifiComponent } from './infrastructure-nifi/infrastructure-nifi.component';
import { InfrastructureNifiEditComponent } from './infrastructure-nifi/infrastructure-nifi-edit.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@NgModule({
  declarations: [
    InfrastructureComponent,
    InfrastructureOverviewComponent,
    InfrastructureMqttComponent,
    InfrastructureMqttEditComponent,
    InfrastructureZookeeperComponent,
    InfrastructureZookeeperEditComponent,
    InfrastructureVirtualizationComponent,
    InfrastructureVirtualizationEditComponent,
    InfrastructureNifiComponent,
    InfrastructureNifiEditComponent,
  ],
  imports: [
    CommonModule,
    InfrastructureRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
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
    ContainersModule,
    MatProgressSpinnerModule,
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class InfrastructureModule {
}
