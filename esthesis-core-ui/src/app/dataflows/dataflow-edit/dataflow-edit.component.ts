import {Component, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {dataflows} from "../dto/dataflow-definitions/dataflow-definition";
import {ActivatedRoute, Router} from "@angular/router";
import {DataflowsService} from "../dataflows.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";

@Component({
  selector: "app-dataflow-edit",
  templateUrl: "./dataflow-edit.component.html"
})
export class DataflowEditComponent extends SecurityBaseComponent implements OnInit {
  id!: string;
  type!: string;
  form = new FormGroup({});
  fields: FormlyFieldConfig[] = [];
  model = {};
  dataflow!: any;

  constructor(private readonly route: ActivatedRoute,
    private readonly dataflowService: DataflowsService,
    private readonly utilityService: UtilityService, private readonly dialog: MatDialog,
    private readonly router: Router,
    private readonly qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW, route.snapshot.paramMap.get("id"));
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id")!;
    this.type = this.route.snapshot.paramMap.get("type")!;

    // Find the form definition for this dataflow and extract dataflow details.
    this.dataflow = dataflows.find(d => d.type === this.type);
    this.fields = this.dataflow.fields || [];
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

    // Enrich model with manual entries.
    this.model = {
      type: this.type
    };

    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      // @ts-ignore
      this.model.id = this.id;
      this.dataflowService.findById(this.id).subscribe({
        next: (reply) => {
          this.model = reply;
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("Could not fetch dataflow.", err);
        }
      });
    }
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
        this.dataflowService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Dataflow component successfully deleted.");
            this.router.navigate(["dataflow"]);
          }, error: err => {
            this.utilityService.popupErrorWithTraceId(
              "There was an error trying to delete dataflow.", err);
          }
        });
      }
    });
  }

  save() {
    if (!this.form.valid) {
      this.utilityService.popupError("Mandatory dataflow configuration is missing.");
    } else {
      this.dataflowService.save(JSON.stringify(this.model)).subscribe({
        next: () => {
          this.router.navigate(["dataflow"]);
        }, error: (err) => {
          if (err.status === 400) {
            const validationErrors = err.error;
            if (validationErrors) {
              this.qFormValidation.applyFormlyValidationErrors(this.form, validationErrors.violations);
              this.utilityService.popupError("Please correct your data and try again.");
            }
          } else if (err.status === 401) {
            this.utilityService.popupErrorWithTraceId(err.error.errorMessage, err);
          } else {
            this.utilityService.popupError("There was an error trying to save this dataflow.");
          }
        }
      });
    }
  }
}
