import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/component/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {UtilityService} from "../../shared/service/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../shared/service/form-validation.service";
import {DataflowService} from "../dataflow.service";
import {TagDto} from "../../dto/tag-dto";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {TagService} from "../../tags/tag.service";
import {DataflowDto} from "../../dto/dataflow/dataflow-dto";

@Component({
  selector: "app-dataflow-mqtt-client",
  templateUrl: "./dataflow-mqtt-client.component.html",
  styleUrls: ["./dataflow-mqtt-client.component.scss"]
})
export class DataflowMqttClientComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, private route: ActivatedRoute, private qForms: QFormsService,
    private router: Router, private utilityService: UtilityService, private dialog: MatDialog,
    private qFormValidation: QFormValidationEEService, private tagService: TagService,
    private dataflowService: DataflowService) {
    super();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    // Form setup.
    this.form = this.fb.group({
      id: [{value: "", disabled: true}],
      name: [{value: "", disabled: false}, [Validators.maxLength(1024)]],
      description: [{value: "", disabled: false}, [Validators.maxLength(4096)]],
      url: [{value: "", disabled: false}, [Validators.maxLength(1024)]],
      status: ["", [Validators.required, Validators.maxLength(5)]],
      minPods: [1, [Validators.required, Validators.min(0)]],
      maxPods: [1, [Validators.required, Validators.max(100)]],
      tags: [[]],
      topicPing: [{
        value: "esthesis/ping/:hardwareId", disabled: false
      }, [Validators.maxLength(1024)]],
      topicTelemetry: [{
        value: "esthesis/telemetry/:hardwareId", disabled: false
      }, [Validators.maxLength(1024)]],
      topicMetadata: [{
        value: "esthesis/metadata/:hardwareId", disabled: false
      }, [Validators.maxLength(1024)]],
      topicControlRequest: [{
        value: "esthesis/control/request/:hardwareId", disabled: false
      }, [Validators.maxLength(1024)]],
      topicControlReply: [{
        value: "esthesis/control/reply/:hardwareId", disabled: false
      }, [Validators.maxLength(1024)]],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.dataflowService.findById(this.id).subscribe(dataflowDto => {
        // Patch form elements common to all types of dataflow components.
        this.form.patchValue(dataflowDto);

        // Patch form elements specific to this type of dataflow component.
        this.form.patchValue(JSON.parse(dataflowDto.configuration!));
      });
    }

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });
  }

  save() {
    const form = this.qForms.cleanupData(this.form.getRawValue());
    const dataflowDto = new DataflowDto();
    dataflowDto.id = form.id;
    dataflowDto.description = form.description;
    dataflowDto.name = form.name;
    dataflowDto.type = this.appConstants.DATAFLOW_TYPE.MQTT_CLIENT;
    dataflowDto.status = form.status;
    dataflowDto.minPods = form.minPods;
    dataflowDto.maxPods = form.maxPods;
    dataflowDto.configuration = JSON.stringify({
      "tags": form.tags,
      "topicPing": form.topicPing,
      "topicTelemetry": form.topicTelemetry,
      "topicMetadata": form.topicMetadata,
      "topicControlRequest": form.topicControlRequest,
      "topicControlReply": form.topicControlReply
    });

    this.dataflowService.save(dataflowDto).subscribe({
      next: next => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("Dataflow component was successfully created.");
        } else {
          this.utilityService.popupSuccess("Dataflow component was successfully edited.");
        }
        this.router.navigate(["dataflow"]);
      }, error: error => {
        if (error.status == 400) {
          let validationErrors = error.error;
          if (validationErrors) {
            // @ts-ignore
            this.qFormValidation.validateForm(this.form, validationErrors.violations);
          }
        } else {
          this.utilityService.popupError("There was an error trying to save this dataflow.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete dataflow component",
        question: "Do you really want to delete this dataflow component?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.dataflowService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess("Dataflow component successfully deleted.");
          this.router.navigate(["dataflow"]);
        });
      }
    });
  }

  minPodsChanged() {
    if (this.form.controls.minPods.value > this.form.controls.maxPods.value) {
      this.form.controls.maxPods.setValue(this.form.controls.minPods.value);
    }
  }

  maxPodsChanged() {
    if (this.form.controls.maxPods.value < this.form.controls.minPods.value) {
      this.form.controls.minPods.setValue(this.form.controls.maxPods.value);
    }
  }
}
