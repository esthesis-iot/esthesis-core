import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {BaseComponent} from '../../../shared/component/base-component';
import {QFormsService} from '@qlack/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../../../shared/service/utility.service';
import {ProvisioningService} from '../../../provisioning/provisioning.service';
import {ProvisioningDto} from '../../../dto/provisioning-dto';
import {CampaignMemberDto} from '../../../dto/campaign-member-dto';
import {DevicesService} from '../../../devices/devices.service';
import {DeviceDto} from '../../../dto/device-dto';
import 'rxjs/add/operator/debounceTime';
import {TagDto} from '../../../dto/tag-dto';
import {TagService} from '../../../tags/tag.service';
import {CampaignsService} from '../../campaigns.service';
import {CampaignDto} from '../../../dto/campaign-dto';
import {CampaignConditionDto} from '../../../dto/campaign-condition-dto';
import * as _ from "lodash"
import {AppConstants} from '../../../app.constants';
import {OkCancelModalComponent} from '../../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-campaign-edit',
  templateUrl: './campaign-edit.component.html',
  styleUrls: ['./campaign-edit.component.scss']
})
export class CampaignEditComponent extends BaseComponent implements OnInit {
  // Campaign details form.
  form!: FormGroup;
  // The id of the form currently being processed.
  id?: number;
  // The list of currently active provisioning packages.
  provisioningPackages?: ProvisioningDto[];
  // A helper auto-complete container for devices matching the user's search input.
  searchDevices?: DeviceDto[];
  // The list of currently available tags.
  availableTags: TagDto[] | undefined;
  memberGroups: any;
  // Validation errors.
  errorsMain?: string[];
  errorsDevices?: string[];
  errorsConditions?: string[];

  constructor(private fb: FormBuilder, public utilityService: UtilityService,
              private qForms: QFormsService, private provisioningService: ProvisioningService,
              private route: ActivatedRoute, private router: Router,
              private utilService: UtilityService, private deviceService: DevicesService,
              private tagService: TagService, private campaignService: CampaignsService,
              private dialog: MatDialog) {
    super();
  }

  ngOnInit(): void {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: ['', []],
      state: ['', []],
      name: ['', []],
      description: ['', []],
      scheduleDate: ['', []],
      scheduleHour: ['', []],
      scheduleMinute: ['', []],
      type: ['', []],
      commandName: ['', []],
      commandArguments: ['', []],
      provisioningPackageId: ['', []],
      conditions: this.fb.array([]),
      members: [[]],

      searchByHardwareId: ['', []],
      searchByTags: ['', []],
    });

    // Monitor campaign type to fetch provisioning packages.
    this.form.get("type")!.valueChanges.subscribe(onNext => {
      if (onNext === this.constants.CAMPAIGN.TYPE.PROVISIONING) {
        this.getProvisioningPackages();
      }
    });

    // Monitor for changes in search by hardware Id input.
    this.form.get("searchByHardwareId")!.valueChanges.debounceTime(500).subscribe(onNext => {
      if (onNext && onNext.trim() !== "") {
        this.deviceService.findDeviceByPartialHardwareId(onNext).subscribe(
          onNext => {
            if (onNext && onNext.length > 0) {
              this.searchDevices = onNext;
            } else {
              this.searchDevices = [];
            }
          }
        );
      } else {
        this.searchDevices = [];
      }
    });

    // Get available tags.
    this.tagService.getAll().subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.campaignService.get(this.id).subscribe(onNext => {
        this.form.patchValue(onNext);
        // Update device membership into this campaign.
        this.memberGroups = this.getMemberGroups();
        this.form.patchValue({"conditions": []});
        onNext.conditions.forEach(c => {
          // @ts-ignore
          this.form.controls['conditions'].push(this.createCondition(c));
        });
      });
    }
  }

  private createCondition(campaignConditionDto: CampaignConditionDto) {
    return this.fb.group({
      id: campaignConditionDto.id,
      type: campaignConditionDto.type,
      target: campaignConditionDto.target,
      stage: campaignConditionDto.stage,
      scheduleDate: campaignConditionDto.scheduleDate,
      scheduleHour: campaignConditionDto.scheduleHour,
      scheduleMinute: campaignConditionDto.scheduleMinute,
      operation: campaignConditionDto.operation,
      value: campaignConditionDto.value,
      propertyName: campaignConditionDto.propertyName,
    });
  }

  addCondition(type: number) {
    // @ts-ignore
    this.form.controls['conditions'].push(
      this.createCondition(new CampaignConditionDto(type)));
  }

  getConditions() {
    // @ts-ignore
    return this.form.get('conditions')['controls'];
  }

  getIcon(type: number): string | undefined {
    switch (type) {
      case this.constants.CAMPAIGN.CONDITION.TYPE.DATETIME:
        return "alarm";
      case this.constants.CAMPAIGN.CONDITION.TYPE.FAILURE:
        return "flash_off";
      case this.constants.CAMPAIGN.CONDITION.TYPE.PAUSE:
        return "pause";
      case this.constants.CAMPAIGN.CONDITION.TYPE.PROPERTY:
        return "tune";
      case this.constants.CAMPAIGN.CONDITION.TYPE.SUCCESS:
        return "outlined_flag";
      case this.constants.CAMPAIGN.CONDITION.TYPE.BATCH:
        return "layers";
      default:
        return undefined;
    }
  }

  getProvisioningPackages() {
    this.provisioningService.getAll("state=1&sort=name,packageVersion").subscribe(onNext => {
      this.provisioningPackages = onNext.content;
    })
  }

  /**
   *
   */
  public currentGroup(): number {
    let groupNo;
    if (this.form.get('members')?.value.length == 0) {
      groupNo = 1;
    } else {
      groupNo = (_.maxBy(this.form.get('members')?.value, function (o: CampaignMemberDto) {
        return o.groupOrder;
      }) as CampaignMemberDto).groupOrder;
    }

    return groupNo;
  }

  getMemberGroups() {
    return _(this.form.get('members')?.value).groupBy('groupOrder').values().value();
  }

  /**
   *
   * @param groupNumber
   *          undefined: The entry is added on the last group. If no groups exist, a new group is
   *                     automatically created.
   *         number > 0: The entry is added in the specified group.
   *                  0: The entry is added in a new group.
   */
  addDeviceOrTag(groupNumber?: number) {
    let groupNo: number;
    if (groupNumber == undefined) {
      groupNo = this.currentGroup();
    } else {
      if (groupNumber == 0) {
        groupNo = this.currentGroup() + 1;
      } else {
        groupNo = groupNumber;
      }
    }

    // Add hardware Ids.
    let hardwareId = this.form.get("searchByHardwareId")?.value;
    if (hardwareId && hardwareId !== "") {
      hardwareId = hardwareId.trim();
      // this.deviceService.getDevices(`hardwareId=${hardwareId}`).subscribe(onNext => {
      //   if (onNext.content && onNext.content.length == 1) {
      this.form.get("members")?.value.push(
        new CampaignMemberDto(this.constants.CAMPAIGN.MEMBER_TYPE.DEVICE, hardwareId, groupNo));
      this.utilityService.popupSuccess("Device added successfully.");
      // } else {
      //   this.utilityService.popupError("Could not find a device with the requested hardware
      // Id."); } });
    }

    // Add tags.
    const tags = this.form.get("searchByTags")!.value;
    if (tags && tags.length > 0) {
      const tagString = tags.join(", ");
      this.form.get("members")?.value.push(
        new CampaignMemberDto(this.constants.CAMPAIGN.MEMBER_TYPE.TAG, tagString, groupNo));
    }

    // Clear search boxes.
    this.form.patchValue({
      searchByHardwareId: "",
      searchByTags: ""
    });

    this.memberGroups = this.getMemberGroups();
  }

  removeMember(identifier: string) {
    _.remove(this.form.get("members")!.value, function (o: CampaignMemberDto) {
      return o.identifier === identifier
    });
    this.memberGroups = this.getMemberGroups();
  }

  removeGroup(groupOrder: number) {
    groupOrder++;
    // Remove group members.
    _.remove(this.form.get("members")!.value, function (o: CampaignMemberDto) {
      return o.groupOrder === groupOrder
    });

    // Rearrange groups.
    _.map(this.form.get("members")!.value, function (o: CampaignMemberDto) {
      if (o.groupOrder > groupOrder) {
        o.groupOrder = o.groupOrder - 1;
      }
      return o;
    })

    this.memberGroups = this.getMemberGroups();
  }

  save() {
    // Clear unused entries and errors.
    this.form.get("searchByHardwareId")?.setValue(undefined);
    this.form.get("searchByTags")?.setValue(undefined);

    this.campaignService.save(
      this.qForms.cleanupData(this.form.getRawValue()) as CampaignDto).subscribe(
      onNext => {
        this.errorsMain = undefined;
        this.errorsDevices = undefined;
        this.errorsConditions = undefined;
        this.utilityService.popupSuccess('Campaign successfully saved.');
        //this.router.navigate(['devices']);
      },
      onError => {
        this.errorsMain = onError.error.main;
        this.errorsDevices = onError.error.devices;
        this.errorsConditions = onError.error.conditions;
        this.utilityService.popupError("Campaign can not be created.");
      });
  }

  test(): void {
    for (let i = 1; i < 13; i++) {
      this.form.patchValue({
        searchByHardwareId: "Item " + i
      });
      i % 4 == 0 ? this.addDeviceOrTag(0) : this.addDeviceOrTag();
    }

    this.form.patchValue({
      name: "My camp",
      description: "my desc",
      type: this.constants.CAMPAIGN.TYPE.COMMAND,
      commandName: "mycmd",
      commandArguments: "arg1"
    })

    this.addCondition(AppConstants.CAMPAIGN.CONDITION.TYPE.BATCH);
    // @ts-ignore
    this.form.get("conditions").controls[0].setValue(
      {"operation": null, "propertyName": null, "scheduleDate": null, "scheduleHour": null,
        "scheduleMinute": null, "stage": null, "target": 1,
        "type": AppConstants.CAMPAIGN.CONDITION.TYPE.BATCH, "value": 10}
    );

    this.addCondition(AppConstants.CAMPAIGN.CONDITION.TYPE.DATETIME);
    // @ts-ignore
    this.form.get("conditions").controls[1].setValue(
      {"operation": 1, "propertyName": null, "scheduleDate": new Date(),
        "scheduleHour": "21", "scheduleMinute": "15", "stage": 1,
        "target": 1, "type": AppConstants.CAMPAIGN.CONDITION.TYPE.DATETIME, "value": null}
    );

    this.addCondition(AppConstants.CAMPAIGN.CONDITION.TYPE.FAILURE);
    // @ts-ignore
    this.form.get("conditions").controls[2].setValue(
      {"operation": 3, "propertyName": null, "scheduleDate": null,
        "scheduleHour": null, "scheduleMinute": null, "stage": 1,
        "target": 1, "type": AppConstants.CAMPAIGN.CONDITION.TYPE.FAILURE, "value": 10}
    );

    this.addCondition(AppConstants.CAMPAIGN.CONDITION.TYPE.PAUSE);
    // @ts-ignore
    this.form.get("conditions").controls[3].setValue(
      {"operation": 6, "propertyName": null, "scheduleDate": null,
        "scheduleHour": null, "scheduleMinute": null, "stage": 1,
        "target": 1, "type": AppConstants.CAMPAIGN.CONDITION.TYPE.PAUSE, "value": 10}
    );

    this.addCondition(AppConstants.CAMPAIGN.CONDITION.TYPE.PROPERTY);
    // @ts-ignore
    this.form.get("conditions").controls[4].setValue(
      {"operation": 8, "propertyName": "abc", "scheduleDate": null,
        "scheduleHour": null, "scheduleMinute": null, "stage": 1,
        "target": 1, "type": AppConstants.CAMPAIGN.CONDITION.TYPE.PROPERTY, "value": 10}
    );

    this.addCondition(AppConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS);
    // @ts-ignore
    this.form.get("conditions").controls[5].setValue(
      {"operation": 4, "propertyName": null, "scheduleDate": null,
        "scheduleHour": null, "scheduleMinute": null, "stage": 1,
        "target": 1, "type": AppConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS, "value": 10}
    );
  }

  removeCondition(i: number) {
    (this.form.controls['conditions'] as FormArray).removeAt(i);
  }

  start() {
    // this.dialog.open(OkCancelModalComponent, {
    //   data: {
    //     title: "Starting campaign",
    //     question: "Once a campaign is started it can not be further edited. Are you sure you" +
    //       " want to proceed?",
    //     buttons: {
    //       ok: true, cancel: true, reload: false
    //     }
    //   }
    // }).afterClosed().subscribe(result => {
    //   if (result) {
        this.campaignService.startCampaign(this.id!).subscribe(onNext => {
          console.log(onNext);
        });
    //   }
    // });
  }
}
