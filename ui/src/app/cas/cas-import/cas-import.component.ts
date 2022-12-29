import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {QFormsService} from "@qlack/forms";
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpEvent, HttpResponse} from "@angular/common/http";
import {CasService} from "../cas.service";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";

@Component({
  selector: "app-cas-import",
  templateUrl: "./cas-import.component.html",
  styleUrls: []
})
export class CasImportComponent extends BaseComponent implements OnInit {
  form!: FormGroup;

  constructor(private fb: FormBuilder, private casService: CasService,
    private qForms: QFormsService,
    private route: ActivatedRoute, private router: Router,
    private dialog: MatDialog,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Set up the form.
    this.form = this.fb.group({
      name: ["", [Validators.required]],
      publicKey: ["", [Validators.required]],
      privateKey: ["", [Validators.required]],
      certificate: ["", [Validators.required]],
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
    this.casService.import(this.form).subscribe({
      next: (event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          if (event.status === 200) {
            this.utilityService.popupSuccess("Certificate Authority restored successfully.");
            this.router.navigate(["cas"]);
          } else {
            this.utilityService.popupError("Could not import Certificate Authority, please try again.");
          }
        }
      }, error: (error: any) => {
        this.utilityService.popupErrorWithTraceId("Could not import Certificate Authority, please try again.", error.error);
      }
    });
  }
}
