import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../shared/component/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {QFormsService} from '@eurodyn/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../../shared/service/utility.service';
import {OkCancelModalComponent} from '../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {NiFiDto} from '../../dto/ni-fi-dto';
import {NiFiService} from './nifi.service';
import {SyncService} from './sync.service';

@Component({
  selector: 'app-infrastructure-nifi-edit',
  templateUrl: './infrastructure-nifi-edit.component.html',
  styleUrls: ['./infrastructure-nifi-edit.component.scss']
})
export class InfrastructureNiFiEditComponent extends BaseComponent implements OnInit {
  form: FormGroup;
  id: number;
  nifi: NiFiDto[];
  activeNiFiId = localStorage.getItem('activeNiFi');

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              private qForms: QFormsService,
              private nifiService: NiFiService, private syncService: SyncService, private route: ActivatedRoute,
              private router: Router, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: [''],
      name: ['', [Validators.required, Validators.maxLength(256)]],
      url: ['', [Validators.required, Validators.maxLength(2048)]],
      description: ['', [Validators.maxLength(4096)]],
      state: ['', [Validators.required, Validators.maxLength(5)]],
      synced: [{value: '', disabled: true}],
      lastChecked: [{value: '', disabled: true}]
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== 0) {
      this.nifiService.getAll().subscribe(value => {

      });
      this.nifiService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
      });
    }
  }

  save() {
    if (this.form.get('state').value && this.activeNiFiId != null && parseInt(
      this.activeNiFiId) != this.id) {
      this.activate();
    } else {
      this.submit();
    }
  }

  submit() {
    this.nifiService.save(this.qForms.cleanupForm(this.form)).subscribe(onNext => {
      this.utilityService.popupSuccess('NiFi server successfully saved.');
      this.router.navigate(['infra'], {fragment: 'nifi'});
    })
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
        if (this.form.get('synced').value) {
          this.deleteWF().afterClosed().subscribe(result => {
            if (result) {
              this.syncService.deleteWorkflow().subscribe(value => {
                this.deleteNiFiInstance();
              })
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
      if (this.activeNiFiId != null && parseInt(this.activeNiFiId) == this.id) {
        localStorage.removeItem('activeNiFi');
      }
      this.router.navigate(['infra'], {fragment: 'nifi'});
    });
  }

  private deleteWF(): MatDialogRef<OkCancelModalComponent> {
    const dialogRefWF = this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete NiFi Workflow',
        question: 'Do you also want to delete the Workflow from the NiFi instance?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    });

    return dialogRefWF;
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
        this.form.get('state').setValue(false);
      }
    });
  }

  sync() {
    this.syncService.sync().subscribe(
      value => this.router.navigate(['infra'], {fragment: 'nifi'}));
  }

}
