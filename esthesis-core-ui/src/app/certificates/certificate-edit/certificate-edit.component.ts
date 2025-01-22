import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {CertificatesService} from "../certificates.service";
import {AppConstants} from "../../app.constants";
import {CaDto} from "../../cas/dto/ca-dto";
import {CasService} from "../../cas/cas.service";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {CertificateDto} from "../dto/certificate-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-certificate-edit",
  templateUrl: "./certificate-edit.component.html",
  styleUrls: []
})
export class CertificateEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;
  issuers: CaDto[] | undefined;
  // Expose application constants.
  constants = AppConstants;

  constructor(private readonly fb: FormBuilder,
    private readonly certificatesService: CertificatesService,
    private readonly route: ActivatedRoute, private readonly router: Router,
    private readonly caService: CasService,
    private readonly utilityService: UtilityService, private readonly dialog: MatDialog) {
    super(AppConstants.SECURITY.CATEGORY.CERTIFICATES, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      cn: [{
        value: null,
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required, Validators.maxLength(256)]],
      name: [{
        value: null,
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required, Validators.maxLength(256)]],
      san: [{
        value: null,
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.maxLength(1024)]],
      validity: [{
        value: null,
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required]],
      issuer: [{value: null, disabled: this.id !== AppConstants.NEW_RECORD_ID}, []]
    });

    // Fill dropdowns.
    this.caService.getEligibleForSigning().subscribe(onNext => {
      this.issuers = onNext;
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== AppConstants.NEW_RECORD_ID) {
      this.certificatesService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  save() {
    this.certificatesService.save(this.form.getRawValue() as CertificateDto).subscribe({
      next: () => {
        this.utilityService.popupSuccess(
          this.form.value.id ? "Certificate was successfully edited."
            : "Certificate was successfully created.");
        this.router.navigate(["certificates"]);
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete certificate",
        question: "Do you really want to delete this certificate?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.certificatesService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Certificate successfully deleted.");
            this.router.navigate(["certificates"]);
          }
        });
      }
    });
  }

  downloadCertificate() {
    this.certificatesService.download(this.id!, AppConstants.KEY_TYPE.CERTIFICATE);
  }

  downloadPublicKey() {
    this.certificatesService.download(this.id!, AppConstants.KEY_TYPE.PUBLIC_KEY);
  }

  downloadPrivateKey() {
    this.certificatesService.download(this.id!, AppConstants.KEY_TYPE.PRIVATE_KEY);
  }
}
