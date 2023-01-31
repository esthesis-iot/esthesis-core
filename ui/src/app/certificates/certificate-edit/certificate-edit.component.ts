import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {CertificatesService} from "../certificates.service";
import {AppConstants} from "../../app.constants";
import {CaDto} from "../../cas/dto/ca-dto";
import {CasService} from "../../cas/cas.service";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {CertificateDto} from "../dto/certificate-dto";

@Component({
  selector: "app-certificate-edit",
  templateUrl: "./certificate-edit.component.html",
  styleUrls: []
})
export class CertificateEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;
  issuers: CaDto[] | undefined;
  // Expose application constants.
  constants = AppConstants;

  constructor(private fb: FormBuilder, private certificatesService: CertificatesService,
    private qForms: QFormsService,
    private route: ActivatedRoute, private router: Router, private caService: CasService,
    private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [""],
      cn: [{
        value: "",
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required, Validators.maxLength(256)]],
      name: [{
        value: "",
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required, Validators.maxLength(256)]],
      san: [{
        value: "",
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.maxLength(1024)]],
      validity: [{
        value: "",
        disabled: this.id !== AppConstants.NEW_RECORD_ID
      }, [Validators.required]],
      issuer: [{value: "", disabled: this.id !== AppConstants.NEW_RECORD_ID}, []]
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
