import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {InfrastructureRoutingModule} from "./infrastructure-routing.module";
import {InfrastructureComponent} from "./infrastructure.component";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatButtonModule} from "@angular/material/button";
import {ComponentsModule} from "../shared/components/components.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {
  InfrastructureMqttListComponent
} from "./infrastructure-mqtt/infrastructure-mqtt-list/infrastructure-mqtt-list.component";
import {
  InfrastructureMqttEditComponent
} from "./infrastructure-mqtt/infrastructure-mqtt-edit/infrastructure-mqtt-edit.component";
import {CdkTableModule} from "@angular/cdk/table";
import {
  CdkFixedSizeVirtualScroll,
  CdkVirtualForOf,
  CdkVirtualScrollViewport
} from "@angular/cdk/scrolling";
import {MtxSelect} from "@ng-matero/extensions/select";

@NgModule({
  declarations: [
    InfrastructureComponent,
    InfrastructureMqttEditComponent,
    InfrastructureMqttListComponent
  ],
  imports: [
    CommonModule,
    InfrastructureRoutingModule,
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
    ComponentsModule,
    MatProgressSpinnerModule,
    FontAwesomeModule,
    CdkTableModule,
    CdkVirtualScrollViewport,
    CdkFixedSizeVirtualScroll,
    CdkVirtualForOf,
    MtxSelect,
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class InfrastructureModule {
}
