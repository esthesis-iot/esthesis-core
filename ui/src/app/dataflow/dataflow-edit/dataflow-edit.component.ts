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

    // Find the form definition for this dataflow.
    this.fields = dataflows.find(d => d.type === this.type)?.fields || [];

    // Enrich model with manual entries.
    this.model = {
      type: this.type
    };
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      // @ts-ignore
      this.model.id = this.id;
    }

    // Replace the tags field with the actual tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.replaceSelectValues(this.fields, "config.tags",
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

  save() {
    // if (!this.form.valid) {
    //   this.utilityService.popupError("Mandatory dataflow configuration is missing.");
    //   return;
    // }

    // Switch all model's values to string.
    const modelText = this.utilityService.deepMap(this.model, (v: any, k: any) => {
      return "" + v;
    });

    this.dataflowService.save(modelText).subscribe({
      next: (reply) => {
        console.log(reply);
      }, error: err => {
        console.log(err);
      }
    });
  }
}
