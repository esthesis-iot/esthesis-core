import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {DataflowRoutingModule} from "./dataflow-routing.module";
import {DataflowComponent} from "./dataflow.component";
import {MatCardModule} from "@angular/material/card";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatTableModule} from "@angular/material/table";
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatButtonModule} from "@angular/material/button";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";
import {DataflowMqttClientComponent} from "./dataflow-mqtt-client/dataflow-mqtt-client.component";
import {ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {A11yModule} from "@angular/cdk/a11y";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSelectModule} from "@angular/material/select";
import {MatSliderModule} from "@angular/material/slider";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";


@NgModule({
  declarations: [
    DataflowComponent,
    DataflowNewComponent,
    DataflowMqttClientComponent,
  ],
  imports: [
    CommonModule,
    DataflowRoutingModule,
    MatCardModule,
    FlexLayoutModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    A11yModule,
    MatIconModule,
    MatTooltipModule,
    MatSelectModule,
    MatSliderModule,
    MatSlideToggleModule
  ]
})
export class DataflowModule {
}
