import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {TagsService} from "../tags.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {TagDto} from "../dto/tag-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";
import {AppConstants} from "../../app.constants";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-tag-edit",
  templateUrl: "./tag-edit.component.html",
  styleUrls: []
})
export class TagEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;

  constructor(private fb: FormBuilder, private tagService: TagsService,
    private route: ActivatedRoute, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog,
    private qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.TAGS, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;
    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [null, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      description: [null, [Validators.maxLength(2048)]]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.tagService.findById(this.id).subscribe({
        next: (tag) => {
          this.form.patchValue(tag);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve this tag.", err);
        }
      });
    }
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());
  }

  save() {
    this.tagService.save(this.form.getRawValue() as TagDto).subscribe({
      next: () => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("Tag was successfully created.");
        } else {
          this.utilityService.popupSuccess("Tag was successfully edited.");
        }
        this.router.navigate(["tags"]);
      }, error: (err) => {
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else if (err.status === 401) {
          this.utilityService.popupErrorWithTraceId(err.error.errorMessage, err);
        } else {
          this.utilityService.popupError("There was an error trying to save this tag.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Tag",
        question: "Do you really want to delete this Tag?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.tagService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Tag successfully deleted.");
            this.router.navigate(["tags"]);
          }
        });
      }
    });
  }
}
