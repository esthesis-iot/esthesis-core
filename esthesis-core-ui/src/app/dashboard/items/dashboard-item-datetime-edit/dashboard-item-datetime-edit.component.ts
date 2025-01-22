import {Component, Inject, OnInit} from "@angular/core";
import {BaseComponent} from "../../../shared/components/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {
  DashboardItemDatetimeConfigurationDto
} from "../../dto/configuration/dashboard-item-datetime-configuration-dto";

@Component({
  selector: 'app-dashboard-item-datetime-edit',
  templateUrl: './dashboard-item-datetime-edit.component.html'
})
export class DashboardItemDatetimeEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    readonly fb: FormBuilder, public dialogRef: MatDialogRef<DashboardItemDatetimeEditComponent>) {
    super();
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemDatetimeConfigurationDto;
    }

    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_date: [configuration?.date],
      configuration_time: [configuration?.time],
      configuration_local: [configuration?.local],
      configuration_server: [configuration?.server],
      configuration_formatDate: [configuration?.formatDate, [Validators.required, Validators.maxLength(255)]],
      configuration_formatTime: [configuration?.formatTime, [Validators.required, Validators.maxLength(255)]],
      configuration_formatDateTime: [configuration?.formatDateTime, [Validators.required, Validators.maxLength(255)]],
    });
  }

  save() {
    let di: DashboardItemDto = {
      id: this.incomingDi.id,
      type: this.incomingDi.type,
      title: this.form.get("title")!.value,
      subtitle: this.form.get("subtitle")!.value,
      columns: this.form.get("columns")!.value,
      index: this.incomingDi.index,
      enabled: this.incomingDi.enabled,
      configuration: JSON.stringify({
        date: this.form.get("configuration_date")!.value,
        time: this.form.get("configuration_time")!.value,
        local: this.form.get("configuration_local")!.value,
        server: this.form.get("configuration_server")!.value,
        formatDate: this.form.get("configuration_formatDate")!.value,
        formatTime: this.form.get("configuration_formatTime")!.value,
        formatDateTime: this.form.get("configuration_formatDateTime")!.value
      } as DashboardItemDatetimeConfigurationDto)
    };
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}

