import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HttpEvent, HttpResponse} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/service/utility.service";
import {CertificatesService} from "../certificates.service";

@Component({
  selector: "app-certificate-import",
  templateUrl: "./certificate-import.component.html",
  styleUrls: []
})
export class CertificateImportComponent implements OnInit {

  form!: FormGroup;

  constructor(private fb: FormBuilder, private certificatesService: CertificatesService,
    private route: ActivatedRoute, private router: Router,
    private dialog: MatDialog,
    private utilityService: UtilityService) {
  }

  ngOnInit() {
    // Set up the form.
    this.form = this.fb.group({
      name: [null, [Validators.required]],
      publicKey: [null, [Validators.required]],
      privateKey: [null, [Validators.required]],
      certificate: [null, [Validators.required]],
    });
  }

  selectPublicKey(event: any) {
    this.form.controls.publicKey.patchValue(event.target.files[0]);
  }

  selectPrivateKey(event: any) {
    this.form.controls.privateKey.patchValue(event.target.files[0]);
  }

  selectCertificate(event: any) {
    this.form.controls.certificate.patchValue(event.target.files[0]);
  }

  import() {
    this.certificatesService.import(this.form).subscribe({
      next: (event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          if (event.status === 200) {
            this.utilityService.popupSuccess("Certificate restored successfully.");
            this.router.navigate(["certificates"]);
          } else {
            this.utilityService.popupError("Something went wrong, please try again.");
          }
        }
      }, error: (error: any) => {
        this.utilityService.popupError(error.error);
      }
    });
  }
}
