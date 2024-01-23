import {Component, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {dataflows} from "../dto/dataflow-definitions/dataflow-definition";
import {ActivatedRoute, Router} from "@angular/router";
import {DataflowsService} from "../dataflows.service";
import {TagsService} from "../../tags/tags.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

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

  constructor(private route: ActivatedRoute, private dataflowService: DataflowsService,
    private tagService: TagsService, private utilityService: UtilityService,
    private dialog: MatDialog, private qForms: QFormsService, private router: Router) {
    super(AppConstants.SECURITY.CATEGORY.DATAFLOW, route.snapshot.paramMap.get("id"));
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id")!;
    this.type = this.route.snapshot.paramMap.get("type")!;

    // Find the form definition for this dataflow and extract dataflow details.
    this.dataflow = dataflows.find(d => d.type === this.type);
    this.fields = this.dataflow.fields || [];

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
          this.utilityService.popupErrorWithTraceId("Could not feth dataflow.", err);
        }
      });
    }
    
    // Replace the namespace field with the actual namespaces.
    this.dataflowService.getNamespaces().subscribe(onNext => {
      this.dataflowService.replaceSelectValues(this.fields, "namespace",
        onNext.map(t => {
          return {label: t, value: t};
        }));
    });

    // Find Docker tags for this dataflow.
    this.dataflowService.getAvailableTags(this.type).subscribe({
      next: tags => {
        this.dataflowService.replaceSelectValues(this.fields, "container-image-version",
          tags.results.map(t => {
            return {label: t.name, value: t.name};
          }));
      }, error: err => {
        this.utilityService.popupErrorWithTraceId(
          "Could not fetch available Docker tags for this image.", err);
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
          this.utilityService.popupErrorWithTraceId("There was an error saving this dataflow.", err);
        }
      });
    }
  }
}
