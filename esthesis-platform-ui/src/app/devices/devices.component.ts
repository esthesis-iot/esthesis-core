import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import {DeviceDto} from '../dto/device-dto';
import {DevicesService} from './devices.service';
import {QFormsService} from '@eurodyn/forms';
import {WebSocketService} from '../services/web-socket.service';
import {Subscription} from 'rxjs';
import {AppConstants} from '../app.constants';
import {BaseComponent} from '../shared/component/base-component';
import {UtilityService} from '../shared/service/utility.service';

@Component({
  selector: 'app-devices',
  templateUrl: './devices.component.html',
  styleUrls: ['./devices.component.scss']
})
export class DevicesComponent extends BaseComponent implements OnInit, AfterViewInit, OnDestroy {
  // Columns to display.
  displayedColumns = ['hardwareId', 'lastSeen', 'createdOn', 'state'];

  // Datasource definition.
  datasource: MatTableDataSource<DeviceDto> = new MatTableDataSource<DeviceDto>();

  // Search filter.
  // TODO
  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  // WebSocket subcription.
  private wsSubscription: Subscription;

  constructor(private fb: FormBuilder, private router: Router,
              private deviceService: DevicesService,
              private qForms: QFormsService, private webSocketService: WebSocketService,
              private utilityService: UtilityService) {
    super();
    this.filterForm = this.fb.group({
      // TODO
    });
  }

  ngOnInit() {
    this.wsSubscription = this.webSocketService.watch(
      AppConstants.WEBSOCKET.TOPIC.DEVICE_REGISTRATION).subscribe(onNext => {
      this.utilityService.popupInfo(onNext.body);
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
        this.sort.start);
    });
  }

  ngOnDestroy(): void {
    this.webSocketService.unwatch(this.wsSubscription);
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Each time the sorting changes, reset the page number.
    this.sort.sortChange.subscribe(onNext => {
      this.paginator.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    // Convert FormGroup to a query string to pass as a filter.
    this.deviceService.getDevices(this.qForms.makeQueryString(this.filterForm, [],
      false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  refreshCurrentData() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.direction);
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  clearFilter() {
    this.filterForm.reset();
  }

}
