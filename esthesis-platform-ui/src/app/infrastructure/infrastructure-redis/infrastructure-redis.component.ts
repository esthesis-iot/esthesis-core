import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../../shared/base-component';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {ContainersPopupService} from '../../shared/containers/containers-popup.service';
import {RedisServerDto} from '../../dto/redis-server-dto';
import {RedisServerService} from './redis-server.service';

@Component({
  selector: 'app-infrastructure-redis',
  templateUrl: './infrastructure-redis.component.html',
  styleUrls: ['./infrastructure-redis.component.scss']
})
export class InfrastructureRedisComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['name', 'ipAddress', 'status'];
  datasource = new MatTableDataSource<RedisServerDto>();

  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private redisServerService: RedisServerService, private qForms: QFormsService,
              private containersPopupService: ContainersPopupService) {
    super();
  }

  ngOnInit() {
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
    this.redisServerService.getAll(this.qForms.appendPagingToFilter(null, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active, this.sort.start);
  }

  deployContainer() {
    this.containersPopupService.deployContainerPopup();
  }

}
