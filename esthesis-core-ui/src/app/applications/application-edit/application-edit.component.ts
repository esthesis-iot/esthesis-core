import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {ApplicationsService} from "../applications.service";
import {
  OkCancelModalComponent
} from "src/app/shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {v4 as uuidv4} from "uuid";
import {ApplicationDto} from "../dto/application-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-application-edit",
  templateUrl: "./application-edit.component.html",
  styleUrls: []
})
export class ApplicationEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id?: string;

  constructor(private readonly fb: FormBuilder,
    private readonly applicationService: ApplicationsService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private dialog: MatDialog,
    private readonly utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.APPLICATION, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [null, [Validators.maxLength(256)]],
      token: [null, [Validators.required, Validators.maxLength(256)]],
      state: [null, [Validators.required]]
    });
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      // Fill-in the form with data if editing an existing item.
      this.applicationService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  generateToken() {
    this.form.controls["token"].setValue(uuidv4());
  }

  save() {
    this.applicationService.save(this.form.getRawValue() as ApplicationDto)
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
