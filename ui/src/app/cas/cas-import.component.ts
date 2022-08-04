import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {QFormsService} from "@qlack/forms";
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpResponse} from "@angular/common/http";
import {CasService} from "./cas.service";
import {BaseComponent} from "../shared/component/base-component";
import {UtilityService} from "../shared/service/utility.service";

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
    // Setup the form.
    this.form = this.fb.group({
      backup: ["", [Validators.required]],
    });
  }

  selectFile(event: any) {
    this.form.controls["backup"].patchValue(event.target.files[0]);
  }

  import() {
    this.casService.import(this.form).subscribe(event => {
      console.log(event);
      if (event instanceof HttpResponse) {
        if (event.status === 200) {
          this.utilityService.popupSuccess("Certificate authority restored successfully.");
          this.router.navigate(["cas"]);
        } else {
          this.utilityService.popupError("Something went wrong, please try again.");
        }
      }
    }, error => {
      this.utilityService.popupError(error.error);
    });
  }
}
