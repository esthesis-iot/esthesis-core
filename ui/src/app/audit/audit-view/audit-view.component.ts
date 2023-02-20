import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {AuditService} from "../audit.service";
import {AuditDto} from "../dto/audit-dto";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-audit-view",
  templateUrl: "./audit-view.component.html",
  styleUrls: ["./audit-view.component.scss"]
})
export class AuditViewComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;
  auditEvent = {} as AuditDto;

  constructor(private fb: FormBuilder, private auditService: AuditService,
    private route: ActivatedRoute, private qForms: QFormsService, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog) {
    super();
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;

    this.auditService.findById(this.id).subscribe({
      next: (auditEvent: AuditDto) => {
        if (auditEvent.valueIn) {
          auditEvent.valueIn = JSON.parse(auditEvent.valueIn);
        }
        if (auditEvent.valueOut) {
          auditEvent.valueOut = JSON.parse(auditEvent.valueOut);
        }
        this.auditEvent = auditEvent;
      }, error: (err: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch audit event", err);
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete audit event",
        question: "Do you really want to delete this audit event?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.auditService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Audit event successfully deleted.");
            this.router.navigate(["audit"]);
          }
        });
      }
    });
  }
}
