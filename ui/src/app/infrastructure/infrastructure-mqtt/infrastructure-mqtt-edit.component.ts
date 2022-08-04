import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, Router} from "@angular/router";
import {MqttServerService} from "./mqtt-server.service";
import {TagService} from "../../tags/tag.service";
import {TagDto} from "../../dto/tag-dto";
import {CertificateDto} from "../../dto/certificate-dto";
import {CertificatesService} from "../../certificates/certificates.service";
import {CasService} from "../../cas/cas.service";
import {CaDto} from "../../dto/ca-dto";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {QFormsService} from "@qlack/forms";
import {MqttServerDto} from "../../dto/mqtt-server-dto";

@Component({
  selector: "app-infrastructure-mqtt-edit",
  templateUrl: "./infrastructure-mqtt-edit.component.html",
  styleUrls: []
})
export class InfrastructureMqttEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id: number | undefined;
  availableTags: TagDto[] | undefined;
  certificates: CertificateDto[] | undefined;
  cas: CaDto[] | undefined;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
    private qForms: QFormsService, private tagService: TagService,
    private mqttServerService: MqttServerService, private route: ActivatedRoute,
    private router: Router, private utilityService: UtilityService,
    private certificatesService: CertificatesService, private casService: CasService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get("id"));

    // Setup the form.
    this.form = this.fb.group({
      id: [""],
      name: ["", [Validators.required, Validators.maxLength(256)]],
      ipAddress: ["", [Validators.required, Validators.maxLength(256)]],
      state: ["", [Validators.required, Validators.maxLength(5)]],
      tags: [[]]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.mqttServerService.findById(this.id).subscribe(onNext => {
        this.form!.patchValue(onNext);
      });
    }

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Get certificates.
    // this.certificatesService.getAll().subscribe(onNext => {
    //   this.certificates = onNext.content;
    // });

    // Get CAs.
    this.casService.find().subscribe(onNext => {
      this.cas = onNext.content;
    });
  }

  save() {
    this.mqttServerService.save(
      this.qForms.cleanupData(this.form.getRawValue()) as MqttServerDto).subscribe(onNext => {
      this.utilityService.popupSuccess("MQTT server successfully saved.");
      this.router.navigate(["infra"], {fragment: "mqtt"});
    });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete MQTT server",
        question: "Do you really want to delete this MQTT server?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.mqttServerService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess("MQTT server successfully deleted.");
          this.router.navigate(["infra"], {fragment: "mqtt"});
        });
      }
    });
  }

  pickCaCertificate(caId: number, control: string) {
    this.casService.findById(caId).subscribe(onNext => {
      this.form!.get(control)!.setValue(onNext.certificate);
    });
  }

  pickCertificate(certificateId: number, control: string) {
    this.certificatesService.findById(certificateId).subscribe(onNext => {
      this.form!.get(control)!.setValue(onNext.certificate);
    });
  }

  pickPrivateKey(certificateId: number, control: string) {
    this.certificatesService.findById(certificateId).subscribe(onNext => {
      this.form!.get(control)!.setValue(onNext.privateKey);
    });
  }

}
