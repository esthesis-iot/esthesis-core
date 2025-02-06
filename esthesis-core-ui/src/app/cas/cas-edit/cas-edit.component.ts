import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {CaDto} from "../dto/ca-dto";
import {AppConstants} from "../../app.constants";
import {CasService} from "../cas.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-cas-edit",
  templateUrl: "./cas-edit.component.html",
  styleUrls: []
})
export class CasEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;
  parents: CaDto[] | undefined;

  constructor(private readonly fb: FormBuilder, protected readonly caService: CasService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly dialog: MatDialog, private readonly utilityService: UtilityService,
    private readonly qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.CA, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      cn: [{
        value: null, disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required, Validators.maxLength(256)]],
      name: [{
        value: null, disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required, Validators.maxLength(256)]],
      validity: [{
        value: null, disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required]],
      parentCaId: [{value: null, disabled: this.id !== AppConstants.NEW_RECORD_ID}],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== AppConstants.NEW_RECORD_ID) {
      this.caService.findById(this.id).subscribe({
        next: ca => {
          this.form.patchValue(ca);
        }, error: error => {
          this.utilityService.popupParsableError(error);
        }
      });
    }

    // Fill dropdowns.
    this.caService.getEligibleForSigning().subscribe(onNext => {
      this.parents = onNext;
    });
  }

  save(): void {
    this.caService.save(this.form.getRawValue() as CaDto).subscribe(
      {
        next: () => {
          this.utilityService.popupSuccess(
            this.form.value.id ? "Certificate Authority was successfully edited."
              : "Certificate Authority was successfully created.");
          this.router.navigate(["cas"]);
        }, error: error => {
          if (error.status === 400) {
            const validationErrors = error.error;
            if (validationErrors) {
              this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
            }
          } else {
            this.utilityService.popupErrorWithTraceId("Could not save Certificate Authority.", error);
          }
        }
      });
  }

  delete(): void {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete CA",
        question: "Do you really want to delete this CA?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.caService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Root CA successfully deleted.");
            this.router.navigate(["cas"]);
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId(
              "Root CA could not be deleted, please try again later.", error);
          }
        });
      }
    });
  }

  downloadCertificate() {
    this.caService.download(this.id!, AppConstants.KEY_TYPE.CERTIFICATE);
  }

  downloadPublicKey() {
    this.caService.download(this.id!, AppConstants.KEY_TYPE.PUBLIC_KEY);
  }

  downloadPrivateKey() {
    this.caService.download(this.id!, AppConstants.KEY_TYPE.PRIVATE_KEY);
  }

}
