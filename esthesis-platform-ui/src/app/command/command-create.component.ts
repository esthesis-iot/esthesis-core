import {Component, OnDestroy, OnInit} from '@angular/core';
import {BaseComponent} from '../shared/component/base-component';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {UtilityService} from '../shared/service/utility.service';
import {Router} from '@angular/router';
import {MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {CommandCreateService} from './command-create.service';
import 'rxjs-compat/add/observable/forkJoin';

@Component({
  selector: 'app-command-create',
  templateUrl: './command-create.component.html',
  styleUrls: ['./command-create.component.scss']
})
export class CommandCreateComponent extends BaseComponent implements OnInit, OnDestroy {
  searchDevicesForm: FormGroup;
  commandForm: FormGroup;
  commands: string[];

  constructor(private formBuilder: FormBuilder, private commandCreateService: CommandCreateService,
              private utilityService: UtilityService, private router: Router,
              public selfDialogRef: MatDialogRef<CommandCreateComponent>) {
    super();
  }

  ngOnInit() {
    // Step 1 form.
    this.searchDevicesForm = this.formBuilder.group({
      hardwareIds: [''],
      tags: [''],
      matches: [0, [Validators.min(1)]],
      devicesMatchedByHardwareIds: [0],
      devicesMatchedByTags: [0]
    });

    // Step 2 form.
    this.commandForm = this.formBuilder.group({
      command: ['', [Validators.required]],
      commandText: [''],
      arguments: [''],
      description: ['']
    });

    // Watch changes on the hardware / tags.
    this.searchDevicesForm.valueChanges.debounceTime(500).subscribe(onNext => {
      Observable.forkJoin([
        this.commandCreateService.findDevicesByHardwareIds(onNext['hardwareIds']),
        this.commandCreateService.findDevicesByTags(onNext['tags'])]).subscribe(results => {
        this.searchDevicesForm.patchValue({
          devicesMatchedByHardwareIds: results[0],
          devicesMatchedByTags: results[1],
          matches: (results[0] + results[1])
        }, {emitEvent: false});
      });
    });
  }

  execute() {
    this.commandCreateService.execute(
      {...this.searchDevicesForm.getRawValue(), ...this.commandForm.getRawValue()}).subscribe(
      () => {
        this.utilityService.popupSuccess('Command dispatched successfully.');
        this.close();
        this.router.navigate(['command']);
      });
  }

  ngOnDestroy(): void {
  }

  close() {
    this.selfDialogRef.close();
  }
}
