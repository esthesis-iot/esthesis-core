import {Component, Input, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {ApplicationsService} from "../applications.service";
import {BaseComponent} from "../../shared/component/base-component";
import {
  OkCancelModalComponent
} from "src/app/shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {UtilityService} from "../../shared/service/utility.service";
import {QFormsService} from "@qlack/forms";
import {v4 as uuidv4} from "uuid";
import {ApplicationDto} from "../dto/application-dto";

@Component({
  selector: "app-application-edit-description",
  templateUrl: "./application-edit-description.component.html",
  styleUrls: []
})
export class ApplicationEditDescriptionComponent extends BaseComponent implements OnInit {
  @Input() id: string | null | undefined;
  form!: FormGroup;

  constructor(private fb: FormBuilder, private applicationService: ApplicationsService,
    private qForms: QFormsService,
    private route: ActivatedRoute, private router: Router, private dialog: MatDialog,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Set up the form.
    this.form = this.fb.group({
      id: [""],
      name: ["", [Validators.maxLength(256)]],
      token: ["", [Validators.required, Validators.maxLength(256)]],
      state: ["", [Validators.required]]
    });

    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      // Fill-in the form with data if editing an existing item.
      this.applicationService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  generateToken() {
    this.form.controls.token.setValue(uuidv4());
  }

  save() {
    this.applicationService.save(this.qForms.cleanupData(this.form.getRawValue()) as ApplicationDto)
    .subscribe({
      next: () => {
        this.utilityService.popupSuccess(this.form.value.id ? "Application was successfully edited."
          : "Application was successfully created.");
        this.router.navigate(["applications"]);
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId("Could not save application, please try again later.", err);
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Application",
        question: "Do you really want to delete this Application?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.applicationService.delete(this.id).subscribe(() => {
          this.utilityService.popupSuccess("Application successfully deleted.");
          this.router.navigate(["applications"]);
        });
      }
    });
  }
}
