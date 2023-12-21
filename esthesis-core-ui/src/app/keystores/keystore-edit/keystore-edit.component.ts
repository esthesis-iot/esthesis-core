import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {MatDialog} from "@angular/material/dialog";
import {DevicesComponent} from "../../devices/devices-list/devices.component";
import {KeystoresService} from "../keystores.service";
import {UtilityService} from "../../shared/services/utility.service";
import {MatTableDataSource} from "@angular/material/table";
import {KeystoreEntryDto} from "../dto/keystore-entry-dto";
import {
  CertificatesListComponent
} from "../../certificates/certificates-list/certificates-list.component";
import {CasListComponent} from "../../cas/cas-list/cas-list.component";
import {KeystoreDto} from "../dto/keystore-dto";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {TagsListComponent} from "../../tags/tags-list/tags-list.component";
import * as _ from "lodash-es";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-keystore-edit",
  templateUrl: "./keystore-edit.component.html",
  styleUrls: []
})
export class KeystoreEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string | null;
  displayedColumns = ["name", "resourceType", "keyType", "password", "actions"];
  dataSource: MatTableDataSource<KeystoreEntryDto> = new MatTableDataSource<KeystoreEntryDto>();
  keystoreTypes: string[] = [];
  // Keystore types which we know don't work.
  excludedKeystoreTypes = ["DKS/SUN", "KeychainStore/Apple"];

  constructor(private fb: FormBuilder, private router: Router,
    private qForms: QFormsService, private dialog: MatDialog,
    private keystoresService: KeystoresService, private utilityService: UtilityService,
    private route: ActivatedRoute) {
    super(AppConstants.SECURITY.CATEGORY.KEYSTORE, route.snapshot.paramMap.get("id"));
  }

  ngOnInit(): void {
    // Get the ID of the requested entry.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: ["", [Validators.required, Validators.maxLength(256)]],
      description: ["", [Validators.maxLength(1024)]],
      password: ["", [Validators.maxLength(1024)]],
      entries: [[]],
      type: ["", Validators.required],
      version: ["0"],
    });

    // Get supported keystores types.
    this.keystoresService.getSupportedKeystoreTypes().subscribe({
      next: (data) => {
        this.keystoreTypes = _.remove(data, (item) => !this.excludedKeystoreTypes.includes(item));
      }, error: (err) => {
        this.utilityService.popupErrorWithTraceId(
          "There was an error trying to retrieve supported keystore types.", err);
      }
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.keystoresService.findById(this.id).subscribe({
        next: (keystore) => {
          this.form.patchValue(keystore);
          this.dataSource = new MatTableDataSource<KeystoreEntryDto>(keystore.entries);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("There was an error trying to retrieve this tag.", err);
        }
      });
    }
  }

  addDevice() {
    const devicesDialogRef = this.dialog.open(DevicesComponent);
    devicesDialogRef.componentInstance.embedded = true;
    devicesDialogRef.afterClosed().subscribe(result => {
      // Add the selected devices to the keystore items.
      if (result) {
        this.form.controls.entries.value.push({
          id: result.id,
          resourceType: this.appConstants.KEYSTORE.ITEM.RESOURCE_TYPE.DEVICE,
          keyType: [this.appConstants.KEYSTORE.ITEM.KEY_TYPE.CERTIFICATE,
            this.appConstants.KEYSTORE.ITEM.KEY_TYPE.PRIVATE_KEY],
          name: result.hardwareId
        });
        this.dataSource = new MatTableDataSource<KeystoreEntryDto>(this.form.controls.entries.value);
      }
    });
  }

  addCertificate() {
    const certDialogRef = this.dialog.open(CertificatesListComponent);
    certDialogRef.componentInstance.embedded = true;
    certDialogRef.afterClosed().subscribe(result => {
      // Add the selected devices to the keystore items.
      if (result) {
        this.form.controls.entries.value.push({
          id: result.id,
          resourceType: this.appConstants.KEYSTORE.ITEM.RESOURCE_TYPE.CERTIFICATE,
          keyType: [this.appConstants.KEYSTORE.ITEM.KEY_TYPE.CERTIFICATE,
            this.appConstants.KEYSTORE.ITEM.KEY_TYPE.PRIVATE_KEY],
          name: result.name
        });
        this.dataSource = new MatTableDataSource<KeystoreEntryDto>(this.form.controls.entries.value);
      }
    });
  }

  addCA() {
    const caDialogRef = this.dialog.open(CasListComponent);
    caDialogRef.componentInstance.embedded = true;
    caDialogRef.afterClosed().subscribe(result => {
      // Add the selected devices to the keystore items.
      if (result) {
        this.form.controls.entries.value.push({
          id: result.id,
          resourceType: this.appConstants.KEYSTORE.ITEM.RESOURCE_TYPE.CA,
          keyType: [this.appConstants.KEYSTORE.ITEM.KEY_TYPE.CERTIFICATE,
            this.appConstants.KEYSTORE.ITEM.KEY_TYPE.PRIVATE_KEY],
          name: result.name
        });
        this.dataSource = new MatTableDataSource<KeystoreEntryDto>(this.form.controls.entries.value);
      }
    });
  }

  save() {
    const formValue = this.form.getRawValue() as KeystoreDto;
    formValue.version = formValue.version + 1;
    formValue.entries = this.dataSource.data;

    this.keystoresService.save(formValue).subscribe({
      next: () => {
        this.utilityService.popupSuccess(this.form.value.id ? "Keystore was successfully edited."
          : "Keystore was successfully created.");
        this.router.navigate(["keystores"]);
      }
    });
  }

  toggleItem(array: any[], item: any) {
    const index = array.indexOf(item);
    if (index !== -1) {
      array.splice(index, 1);
    } else {
      array.push(item);
    }
  }

  removeItem(array: KeystoreEntryDto[], element: KeystoreEntryDto) {
    array.forEach((item, index) => item.id === element.id && array.splice(index, 1));
    this.dataSource = new MatTableDataSource<KeystoreEntryDto>(this.form.controls.entries.value);
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Keystore",
        question: "Do you really want to delete this keystore?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.keystoresService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Keystore successfully deleted.");
            this.router.navigate(["keystores"]);
          }
        });
      }
    });
  }

  download() {
    this.keystoresService.download(this.id!);
  }

  addTag() {
    const tagDialogRef = this.dialog.open(TagsListComponent);
    tagDialogRef.componentInstance.embedded = true;
    tagDialogRef.afterClosed().subscribe(result => {
      // Add the selected devices to the keystore items.
      if (result) {
        this.form.controls.entries.value.push({
          id: result.id,
          resourceType: this.appConstants.KEYSTORE.ITEM.RESOURCE_TYPE.TAG,
          keyType: [this.appConstants.KEYSTORE.ITEM.KEY_TYPE.CERTIFICATE,
            this.appConstants.KEYSTORE.ITEM.KEY_TYPE.PRIVATE_KEY],
          name: result.name
        });
        this.dataSource = new MatTableDataSource<KeystoreEntryDto>(this.form.controls.entries.value);
      }
    });
  }
}
