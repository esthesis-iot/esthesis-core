import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {HttpEvent, HttpResponse} from "@angular/common/http";
import {CasService} from "../cas.service";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-cas-import",
  templateUrl: "./cas-import.component.html",
  styleUrls: []
})
export class CasImportComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  publicKeyfile: File | null = null;
  privateKeyfile: File | null = null;
  certificateFile: File | null = null;

  constructor(private fb: FormBuilder, private casService: CasService, private router: Router,
    private utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.CA);
  }

  ngOnInit() {
    // Set up the form.
    this.form = this.fb.group({
      name: [null, [Validators.required]]
    });
  }

  selectPublicKey(event: any) {
    const file: File = event.target?.files[0];
    if (file) {
      this.publicKeyfile = file;
    }
  }

  selectPrivateKey(event: any) {
    const file: File = event.target?.files[0];
    if (file) {
      this.privateKeyfile = file;
    }
  }

  selectCertificate(event: any) {
    const file: File = event.target?.files[0];
    if (file) {
      this.certificateFile = file;
    }
  }

  import() {
    let files = new Map<string, File | null>([
      [AppConstants.KEY_TYPE.PUBLIC_KEY.toLowerCase(), this.publicKeyfile],
      [AppConstants.KEY_TYPE.PRIVATE_KEY.toLowerCase(), this.privateKeyfile],
      [AppConstants.KEY_TYPE.CERTIFICATE.toLowerCase(), this.certificateFile]
    ]);
    this.casService.import(this.form.getRawValue(), files).subscribe({
      next: (event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          if (event.status === 200) {
            this.utilityService.popupSuccess("Certificate authority imported successfully.");
            this.router.navigate(["cas"]);
          } else {
            this.utilityService.popupError("Could not import certificate authority, please try again.");
          }
        }
      }, error: (error: any) => {
        this.utilityService.popupErrorWithTraceId("Could not import certificate authority, please try again.", error.error);
      }
    });
  }
}
