import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../base-component';
import {CommandService} from './command.service';
import {Observable} from 'rxjs';
import 'rxjs-compat/add/observable/forkJoin';
import {UtilityService} from '../../service/utility.service';
import {ActivatedRoute, Router} from '@angular/router';
import {MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-command',
  templateUrl: './command.component.html',
  styleUrls: ['./command.component.scss']
})
export class CommandComponent extends BaseComponent implements OnInit, OnDestroy {
  searchDevicesForm: FormGroup;
  commandForm: FormGroup;
  commands: string[];

  constructor(private formBuilder: FormBuilder, private commandService: CommandService,
              private utilityService: UtilityService, private router: Router,
              public selfDialogRef: MatDialogRef<CommandComponent>) {
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
        this.commandService.findDevicesByHardwareIds(onNext['hardwareIds']),
        this.commandService.findDevicesByTags(onNext['tags'])]).subscribe(results => {
        this.searchDevicesForm.patchValue({
          devicesMatchedByHardwareIds: results[0],
          devicesMatchedByTags: results[1],
          matches: (results[0] + results[1])
        }, {emitEvent: false});
      });
    });

    // Fetch available commands.
    this.commandService.findCommands().subscribe(onNext => {
      // this.commands = onNext.map(value => _.startCase(value));
      this.commands = onNext;
    });
  }

  execute() {
    this.commandService.execute(
      {...this.searchDevicesForm.getRawValue(), ...this.commandForm.getRawValue()}).subscribe(
      onNext => {
        this.utilityService.popupSuccess("Command dispatched successfully.");
        // this.router.navigate(['control']);
      });
  }

  ngOnDestroy(): void {
  }

  close() {
    this.selfDialogRef.close();
  }
}
