import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {ZookeeperServerDto} from '../../dto/zookeeper-server-dto';
import {ZookeeperServerService} from './zookeeper-server.service';
import {BaseComponent} from '../../shared/component/base-component';
import {ContainersPopupService} from '../../shared/component/containers/containers-popup.service';

@Component({
  selector: 'app-infrastructure-zookeeper',
  templateUrl: './infrastructure-zookeeper.component.html',
  styleUrls: ['./infrastructure-zookeeper.component.scss']
})
export class InfrastructureZookeeperComponent extends BaseComponent
  implements OnInit, AfterViewInit {
  columns = ['name', 'ipAddress', 'status'];
  datasource = new MatTableDataSource<ZookeeperServerDto>();

  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private zookeeperServerService: ZookeeperServerService, private qForms: QFormsService,
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
    this.zookeeperServerService.getAll(
      this.qForms.appendPagingToFilter(null, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  deployContainer() {
    this.containersPopupService.deployContainerPopup();
  }

}
