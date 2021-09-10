import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../shared/component/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../../shared/service/utility.service';
import {OkCancelModalComponent} from '../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {NiFiDto} from '../../dto/ni-fi-dto';
import {NiFiService} from './nifi.service';
import {SyncService} from './sync.service';
import {QFormsService} from '@qlack/forms';

@Component({
  selector: 'app-infrastructure-nifi-edit',
  templateUrl: './infrastructure-nifi-edit.component.html',
  styleUrls: []
})
export class InfrastructureNiFiEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id!: number;
  nifi: NiFiDto[] | undefined;
  activeNiFiId: any;
  synced: boolean | undefined;
  lastChecked: any;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService,
              private nifiService: NiFiService, private syncService: SyncService,
              private route: ActivatedRoute,
              private router: Router, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    this.nifiService.getActive().subscribe(value => {
      this.activeNiFiId = value?.id;
    });

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      url: ['', [Validators.required, Validators.maxLength(2048)]],
      dtUrl: ['', [Validators.maxLength(2048)]],
      description: ['', [Validators.maxLength(4096)]],
      state: ['', [Validators.required, Validators.maxLength(5)]],
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== 0) {
      this.nifiService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
        this.synced = onNext.synced;
        this.lastChecked = onNext.lastChecked;
      });
    }
  }

  save() {
    if (this.form.get('state')!.value && this.activeNiFiId != null && this.activeNiFiId !== this.id) {
      this.activate();
    } else {
      this.submit();
    }
  }

  submit() {
    this.nifiService.save(this.qForms.cleanupData(this.form.getRawValue()) as NiFiDto).subscribe(
      onNext => {
        this.utilityService.popupSuccess('NiFi server successfully saved.');
        this.router.navigate(['infra'], {fragment: 'nifi'});
      });
  }

  delete() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete NiFi server',
        question: 'Do you really want to delete this NiFi server?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.synced) {
          this.deleteWF().afterClosed().subscribe(deleteResult => {
            if (deleteResult) {
              this.syncService.deleteWorkflow().subscribe(() => {
                this.deleteNiFiInstance();
              }, error => {
                this.utilityService.popupError(error?.error?.message);
              });
            } else {
              this.deleteNiFiInstance();
            }
          });
        } else {
          this.deleteNiFiInstance();
        }
      }
    });
  }

  private deleteNiFiInstance() {
    this.nifiService.delete(this.id).subscribe(onNext => {
      this.utilityService.popupSuccess('NiFi server successfully deleted.');
      this.router.navigate(['infra'], {fragment: 'nifi'});
    });
  }

  private deleteWF(): MatDialogRef<OkCancelModalComponent> {
    return this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete NiFi Workflow',
        question: 'Do you also want to delete the Workflow from the NiFi instance?',
        okValue: 'YES',
        cancelValue: 'NO',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
  }

  activate() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Activate NiFi server',
        question: 'By activating this NiFi server, the previously active server will be' +
          ' deactivated. Do you wish to proceed?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.submit();
      } else {
        this.form.get('state')!.setValue(false);
      }
    });
  }

  sync() {
    this.syncService.sync().subscribe(
      value => this.router.navigate(['infra'], {fragment: 'nifi'}));
  }

  clearQueues() {
    const dialogRef = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Clear Queues',
        question: 'Do you wish to proceed with clearing all queues in NiFi?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.syncService.clearQueues().subscribe(value => {
          this.utilityService.popupSuccess("All queues have been cleared.");
        }, error => {
          this.utilityService.popupSuccess(error?.error?.message);
        });
      }
    });
  }
}
