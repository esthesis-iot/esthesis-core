import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {BaseComponent} from "../../shared/components/base-component";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {InfrastructureMqttService} from "./infrastructure-mqtt.service";
import {InfrastructureMqttDto} from "./dto/Infrastructure-mqtt-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-infrastructure-mqtt-edit",
  templateUrl: "./infrastructure-mqtt-edit.component.html",
  styleUrls: []
})
export class InfrastructureMqttEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagsService: TagsService,
    private infrastructureMqttService: InfrastructureMqttService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService) {
    super();
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

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      this.infrastructureMqttService.findById(this.id).subscribe({
        next: (mqttServer: InfrastructureMqttDto) => {
          this.form!.patchValue(mqttServer);
        }, error: (err: any) => {
          this.utilityService.popupErrorWithTraceId("Could not fetch MQTT server.", err);
        }
      });
    }

    // Get available tags.
    this.tagsService.find("sort=name,asc").subscribe({
      next: (tags: any) => {
        this.availableTags = tags.content;
      }
    });
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
