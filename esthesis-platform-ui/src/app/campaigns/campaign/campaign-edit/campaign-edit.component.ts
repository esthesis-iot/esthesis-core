import {AfterViewInit, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {BaseComponent} from '../../../shared/component/base-component';
import {QFormsService} from '@qlack/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../../../shared/service/utility.service';
import {CampaignConstraintDto} from '../../../dto/campaign-constraint-dto';
import {ProvisioningService} from '../../../provisioning/provisioning.service';
import {ProvisioningDto} from '../../../dto/provisioning-dto';
import {DevicesService} from '../../../devices/devices.service';
import {DeviceDto} from '../../../dto/device-dto';
import 'rxjs/add/operator/debounceTime';
import {TagDto} from '../../../dto/tag-dto';
import {TagService} from '../../../tags/tag.service';
import {CdkDragDrop, moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';
import {CampaignsService} from '../../campaigns.service';
import {CampaignDto} from '../../../dto/campaign-dto';

@Component({
  selector: 'app-campaign-edit',
  templateUrl: './campaign-edit.component.html',
  styleUrls: ['./campaign-edit.component.scss']
})
export class CampaignEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  id: number | undefined;
  provisioningPackages?: ProvisioningDto[];
  searchDevices?: DeviceDto[];
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, public utilityService: UtilityService,
              private qForms: QFormsService, private provisioningService: ProvisioningService,
              private route: ActivatedRoute, private router: Router,
              private utilService: UtilityService, private deviceService: DevicesService,
              private tagService: TagService, private campaignService: CampaignsService) {
    super();
  }

  ngOnInit(): void {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      description: ['', []],
      scheduleDate: ['', []],
      scheduleHour: ['', []],
      scheduleMinute: ['', []],
      type: ['', [Validators.required]],
      commandName: ['', []],
      commandArguments: ['', []],
      provisioningPackageId: ['', []],
      constraints: this.fb.array([]),
      devicesAndTags: [[], []],

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
      if (onNext.trim() !== "") {
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
  }

  private createConstraint(campaignConstraintDto: CampaignConstraintDto) {
    return this.fb.group({
      id: [campaignConstraintDto.id],
      type: [campaignConstraintDto.type, [Validators.required]],
      name: [campaignConstraintDto.name],
      target: [campaignConstraintDto.target],
      stage: [campaignConstraintDto.target],
      scheduleDate: [campaignConstraintDto.target],
      scheduleHour: [campaignConstraintDto.target],
      scheduleMinute: [campaignConstraintDto.target],
      conditionType: [campaignConstraintDto.target],
      value: [campaignConstraintDto.target],
      propertyName: [campaignConstraintDto.target],
    });
  }

  addConstraint(type: number) {
    // @ts-ignore
    this.form.controls['constraints'].push(
      this.createConstraint(new CampaignConstraintDto(type)));
  }

  getConstraints() {
    // @ts-ignore
    return this.form.get('constraints')['controls'];
  }

  getIcon(type: number): string | undefined {
    switch (type) {
      case this.constants.CAMPAIGN.CONSTRAINT.TYPE.DATETIME:
        return "alarm";
      case this.constants.CAMPAIGN.CONSTRAINT.TYPE.FAILURE:
        return "flash_off";
      case this.constants.CAMPAIGN.CONSTRAINT.TYPE.PAUSE:
        return "pause";
      case this.constants.CAMPAIGN.CONSTRAINT.TYPE.PROPERTY:
        return "tune";
      case this.constants.CAMPAIGN.CONSTRAINT.TYPE.SUCCESS:
        return "outlined_flag";
      case this.constants.CAMPAIGN.CONSTRAINT.TYPE.BATCH:
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
   * Creates a new campaign group if no groups are already defined and returns the index of the next
   * group to be used in insert operations.
   */
  private nextGroup(): number {
    let groupNo = this.form.get('devicesAndTags')?.value.length;
    if (groupNo == 0) {
      this.groupSplit();
    } else {
      groupNo--;
    }

    return groupNo;
  }

  addDeviceOrTag() {
    // Add hardware Ids.
    const hardwareId = this.form.get("searchByHardwareId")?.value.trim();
    if (hardwareId !== "") {
      // this.deviceService.getDevices(`hardwareId=${hardwareId}`).subscribe(onNext => {
        // if (onNext.content && onNext.content.length == 1) {
          const groupNo = this.nextGroup();
          this.form.get("devicesAndTags")?.value[groupNo].push(`dev_${hardwareId}`);
          this.utilityService.popupSuccess("Device added successfully.");
        // } else {
        //   this.utilityService.popupError("Could not find a device with the requested hardware Id.");
        // }
      // });
    }

    // Add tags.
    const tags = this.form.get("searchByTags")!.value;
    if (tags.length > 0) {
      const tagString = tags.join(", ");
      const groupNo = this.nextGroup();
      this.form.get("devicesAndTags")?.value[groupNo].push(`tag_${tagString}`);
    }

    // Clear search boxes.
    this.form.patchValue({
      searchByHardwareId: "",
      searchByTags: ""
    });
  }

  test(): void {
    for (let i = 0; i<5; i++) {
      this.form.patchValue({
        searchByHardwareId: "Item " + i
      });
      this.addDeviceOrTag();
    }
    this.groupSplit();
    for (let i = 6; i<10; i++) {
      this.form.patchValue({
        searchByHardwareId: "Item " + i
      });
      this.addDeviceOrTag();
    }
    this.groupSplit();
    for (let i = 11; i<15; i++) {
      this.form.patchValue({
        searchByHardwareId: "Item " + i
      });
      this.addDeviceOrTag();
    }
  }

  /**
   * Adds a new group.
   */
  groupSplit() {
    this.form.get('devicesAndTags')!.value.push([]);
  }

  drop(event: CdkDragDrop<number>) {
    console.log(event);
    if (event.previousContainer === event.container) {
      const sourceGroupOrder = event.container.data as number;
      moveItemInArray(this.form.get("devicesAndTags")?.value[sourceGroupOrder], event.previousIndex, event.currentIndex);
    } else {
      const currentGroupOrder = event.previousContainer.data as number;
      const targetGroupOrder = event.container.data as number;
      transferArrayItem(
        this.form.get("devicesAndTags")?.value[currentGroupOrder],
        this.form.get("devicesAndTags")?.value[targetGroupOrder],
        event.previousIndex,
        event.currentIndex);
    }
  }

  removeDeviceOrTag(groupOrder: number, item: string) {
    const index = this.form.get("devicesAndTags")!.value[groupOrder].indexOf(item)
    this.form.get("devicesAndTags")!.value[groupOrder].splice(index, 1);
  }

  removeGroup(groupOrder: number) {
    this.form.get("devicesAndTags")!.value.splice(groupOrder, 1);
  }

  save() {
    // Clear unused entries.
    this.form.get("searchByHardwareId")?.setValue(undefined);
    this.form.get("searchByTags")?.setValue(undefined);

    this.campaignService.save(
      this.qForms.cleanupData(this.form.getRawValue()) as CampaignDto).subscribe(
      onNext => {
        this.utilityService.popupSuccess('Campaign successfully saved.');
        //this.router.navigate(['devices']);
      });
  }
}
