import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {QFormsService} from '@eurodyn/forms';
import {VirtualizationService} from './virtualization.service';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {VirtualizationDto} from '../../dto/virtualization-dto';
import {BaseComponent} from '../../shared/component/base-component';

@Component({
  selector: 'app-infrastructure-virtualization',
  templateUrl: './infrastructure-virtualization.component.html',
  styleUrls: ['./infrastructure-virtualization.component.scss']
})
export class InfrastructureVirtualizationComponent extends BaseComponent
  implements OnInit, AfterViewInit {
  columns = ['name', 'ipAddress', 'status', 'serverType'];
  datasource = new MatTableDataSource<VirtualizationDto>();

  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private virtualizationService: VirtualizationService, private qForms: QFormsService) {
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
    this.virtualizationService.getAll(
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

}
