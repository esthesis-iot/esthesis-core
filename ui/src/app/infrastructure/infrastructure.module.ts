import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {InfrastructureRoutingModule} from "./infrastructure-routing.module";
import {InfrastructureComponent} from "./infrastructure.component";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {DisplayModule} from "../shared/component/display/display.module";
import {InfrastructureMqttComponent} from "./infrastructure-mqtt/infrastructure-mqtt.component";
import {
  InfrastructureMqttEditComponent
} from "./infrastructure-mqtt/infrastructure-mqtt-edit.component";
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

@NgModule({
  declarations: [
    InfrastructureComponent,
    InfrastructureMqttComponent,
    InfrastructureMqttEditComponent
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
