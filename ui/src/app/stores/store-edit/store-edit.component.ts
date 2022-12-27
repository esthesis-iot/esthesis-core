import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/component/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {StoresService} from "../stores.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {CaDto} from "../../cas/dto/ca-dto";
import {UtilityService} from "../../shared/service/utility.service";
import {CasService} from "../../cas/cas.service";
import {CertificatesService} from "../../certificates/certificates.service";
import {CertificateDto} from "../../certificates/dto/certificate-dto";
import {QFormsService} from "@qlack/forms";
import {StoreDto} from "../dto/store-dto";

@Component({
  selector: "app-store-edit",
  templateUrl: "./store-edit.component.html",
  styleUrls: []
})
export class StoreEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;
  cas: CaDto[] | undefined;
  certs: CertificateDto[] | undefined;

  constructor(private fb: FormBuilder, private storesService: StoresService,
    private qForms: QFormsService, private route: ActivatedRoute, private router: Router,
    private dialog: MatDialog, private utilityService: UtilityService,
    private casService: CasService, private certificatesService: CertificatesService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id");

    // Setup the form.
    this.form = this.fb.group({
      id: [""],
      name: ["", [Validators.required, Validators.maxLength(256)]],
      password: ["", [Validators.required, Validators.maxLength(256)]],
      passwordForKeys: [""],
      certCertificates: [""],
      certCas: [""],
      pkCertificates: [""],
      pkCas: [""],
    });

    // Fill form.
    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      this.storesService.findById(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }

    this.casService.find("sort=name,asc").subscribe(onNext => {
      this.cas = onNext.content;
    });

    this.certificatesService.find("sort=name,asc").subscribe(onNext => {
      this.certs = onNext.content;
    });
  }

  save() {
    this.storesService.save(this.qForms.cleanupData(this.form.getRawValue()) as StoreDto).subscribe({
      next: () => {
        this.utilityService.popupSuccess(this.form.value.id ? "Store was successfully edited."
          : "Store was successfully created.");
        this.router.navigate(["stores"]);
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete store",
        question: "Do you really want to delete this store?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.storesService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Store was successfully deleted.");
            this.router.navigate(["stores"]);
          }
        });
      }
    });
  }

  download() {
    this.storesService.download(this.id!);
  }
}
