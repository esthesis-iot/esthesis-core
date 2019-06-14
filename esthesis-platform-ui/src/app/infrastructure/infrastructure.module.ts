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
import {
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule,
  MatTabsModule
} from '@angular/material';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {InfrastructureZookeeperComponent} from './infrastructure-zookeeper/infrastructure-zookeeper.component';
import {InfrastructureZookeeperEditComponent} from './infrastructure-zookeeper/infrastructure-zookeeper-edit.component';
import {DisplayModule} from '../shared/component/display/display.module';
import {ContainersModule} from '../shared/component/containers/containers.module';

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
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class InfrastructureModule {
}
