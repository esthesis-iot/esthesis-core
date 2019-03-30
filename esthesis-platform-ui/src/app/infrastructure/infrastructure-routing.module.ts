import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {InfrastructureComponent} from './infrastructure.component';
import {InfrastructureMqttEditComponent} from './infrastructure-mqtt/infrastructure-mqtt-edit.component';
import {InfrastructureVirtualizationEditComponent} from './infrastructure-virtualization/infrastructure-virtualization-edit.component';
import {InfrastructureRedisEditComponent} from './infrastructure-redis/infrastructure-redis-edit.component';
import {InfrastructureZookeeperEditComponent} from './infrastructure-zookeeper/infrastructure-zookeeper-edit.component';

const routes: Routes = [
  {path: '', component: InfrastructureComponent},
  {path: 'mqtt/:id', component: InfrastructureMqttEditComponent},
  {path: 'virtualization/:id', component: InfrastructureVirtualizationEditComponent},
  {path: 'redis/:id', component: InfrastructureRedisEditComponent},
  {path: 'zookeeper/:id', component: InfrastructureZookeeperEditComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InfrastructureRoutingModule {
}
