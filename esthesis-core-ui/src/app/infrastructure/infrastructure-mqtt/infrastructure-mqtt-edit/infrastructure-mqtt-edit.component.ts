import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {InfrastructureMqttService} from "../infrastructure-mqtt.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../../shared/services/utility.service";
import {InfrastructureMqttDto} from "../dto/Infrastructure-mqtt-dto";
import {
  OkCancelModalComponent
} from "../../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: "app-infrastructure-mqtt-edit",
  templateUrl: "./infrastructure-mqtt-edit.component.html"
})
export class InfrastructureMqttEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;

  constructor(private readonly fb: FormBuilder, private readonly dialog: MatDialog,
    private readonly infrastructureMqttService: InfrastructureMqttService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.INFRASTRUCTURE, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    // Get the requested id.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [null, [Validators.required, Validators.maxLength(1024)]],
      url: [null, [Validators.required, Validators.maxLength(1024)]],
      active: [null, [Validators.required, Validators.maxLength(1)]],
      tags: [[]]
    });
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      this.infrastructureMqttService.findById(this.id).subscribe({
        next: (mqttServer: InfrastructureMqttDto) => {
          this.form.patchValue(mqttServer);
          this.tagsService.findByIds(mqttServer.tags).subscribe({
            next: (tags: any) => {
              this.loadFilteredTags(tags);
            }});
        }, error: (err: any) => {
          this.utilityService.popupErrorWithTraceId("Could not fetch MQTT server.", err);
        }
      });
    }
  }

  save() {
    this.infrastructureMqttService.save(this.form.getRawValue() as InfrastructureMqttDto).subscribe({
      next: () => {
        this.utilityService.popupSuccess("MQTT server successfully saved.");
        this.router.navigate(["infrastructure"], {fragment: "mqtt"});
      }, error: (err: any) => {
        this.utilityService.popupErrorWithTraceId("Could not save MQTT server.", err);
      }
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete MQTT server",
        question: "Do you really want to delete this MQTT server?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.infrastructureMqttService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("MQTT server successfully deleted.");
            this.router.navigate(["infrastructure"], {fragment: "mqtt"});
          }, error: (err: any) => {
            this.utilityService.popupErrorWithTraceId("Could not delete MQTT server.", err);
          }
        });
      }
    });
  }
}

