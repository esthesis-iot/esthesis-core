import {Component, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {dataflows} from "../dto/dataflow-definition";
import {ActivatedRoute, Router} from "@angular/router";
import {DataflowService} from "../dataflow.service";
import {BaseComponent} from "../../shared/component/base-component";
import {TagService} from "../../tags/tag.service";
import {UtilityService} from "../../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {QFormsService} from "@qlack/forms";

@Component({
  selector: "app-dataflow-edit",
  templateUrl: "./dataflow-edit.component.html",
  styleUrls: ["./dataflow-edit.component.scss"]
})
export class DataflowEditComponent extends BaseComponent implements OnInit {
  id!: string;
  type!: string;
  form = new FormGroup({});
  fields: FormlyFieldConfig[] = [];
  model = {};
  dataflow!: any;

  constructor(private route: ActivatedRoute, private dataflowService: DataflowService,
    private tagService: TagService, private utilityService: UtilityService,
    private dialog: MatDialog, private qForms: QFormsService,
    private router: Router) {
    super();
  }

  private replaceSelectValues(fields: FormlyFieldConfig[], searchElement: string, values: any[]) {
    fields.forEach(f => {
      if (f.key === searchElement) {
        f.props!.options = values;
      }
      f.fieldGroup?.forEach(fg => {
        if (fg.key === searchElement) {
          fg.props!.options = values;
        }
      });
    });
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

    // Replace the tags field with the actual tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.replaceSelectValues(this.fields, "tags",
        onNext.content.map(t => {
          return {label: t.name, value: t.id};
        }));
    });

    // Replace the namespace field with the actual namespaces.
    this.dataflowService.getNamespaces().subscribe(onNext => {
      this.replaceSelectValues(this.fields, "namespace",
        onNext.map(t => {
          return {label: t, value: t};
        }));
    });

    // Find Docker tags for this dataflow.
    this.dataflowService.getAvailableTags(this.type).subscribe({
      next: tags => {
        this.replaceSelectValues(this.fields, "docker",
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
        this.dataflowService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess("Dataflow component successfully deleted.");
          this.router.navigate(["dataflow"]);
        });
      }
    });
  }

  private findInvalidControls() {
    const invalid = [];
    const controls = this.form.controls;
    for (const name in controls) {
      // @ts-ignore
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    return invalid;
  }

  save() {
    if (!this.form.valid) {
      this.utilityService.popupError("Mandatory dataflow configuration is missing.");
    } else {
      this.dataflowService.save(JSON.stringify(this.model)).subscribe({
        next: (reply) => {
          this.router.navigate(["dataflow"]);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error saving this dataflow.", err);
        }
      });
    }
  }
}
