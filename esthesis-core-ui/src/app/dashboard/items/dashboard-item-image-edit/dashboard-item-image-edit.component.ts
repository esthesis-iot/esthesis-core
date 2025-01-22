import {Component, Inject, OnInit} from "@angular/core";
import {BaseComponent} from "../../../shared/components/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {
  DashboardItemImageConfigurationDto
} from "../../dto/configuration/dashboard-item-image-configuration-dto";

@Component({
  selector: 'app-dashboard-item-image-edit',
  templateUrl: './dashboard-item-image-edit.component.html'
})
export class DashboardItemImageEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    readonly fb: FormBuilder, public dialogRef: MatDialogRef<DashboardItemImageEditComponent>) {
    super();
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemImageConfigurationDto;
    }

    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_imageUrl: [configuration?.imageUrl, [Validators.required, Validators.maxLength(1024)]],
      configuration_refresh: [configuration?.refresh, [Validators.required, Validators.pattern('^[0-9]+$')]],
      configuration_height: [configuration?.height, [Validators.required, Validators.pattern('^[0-9]+$')]],
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
        imageUrl: this.form.get("configuration_imageUrl")!.value,
        refresh: this.form.get("configuration_refresh")!.value,
        height: this.form.get("configuration_height")!.value,
      } as DashboardItemImageConfigurationDto)
    };
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}

