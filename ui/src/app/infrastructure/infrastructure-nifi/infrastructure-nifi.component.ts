import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {QFormsService} from '@qlack/forms';
import {NiFiDto} from '../../dto/ni-fi-dto';
import {NiFiService} from './nifi.service';
import {BaseComponent} from '../../shared/component/base-component';

@Component({
  selector: 'app-infrastructure-nifi',
  templateUrl: './infrastructure-nifi.component.html',
  styleUrls: ['./infrastructure-nifi.component.scss']
})
export class InfrastructureNiFiComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['name', 'url', 'description', 'state', 'wfVersion', 'lastChecked'];
  datasource = new MatTableDataSource<NiFiDto>();
  // NiFi workflow version available in the backend.
  backendWfVersion: string | undefined;

  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private nifiService: NiFiService, private qForms: QFormsService) {
    super();
  }

  ngOnInit() {
    // Get latest version of esthesis NiFi workflow available in the backend.
    this.nifiService.getLatestWorkflowVersion().subscribe(onNext => {
      this.backendWfVersion = onNext.version;
    })
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Each time the sorting changes, reset the page number.
    this.sort!.sortChange.subscribe((onNext: { active: string; direction: string; }) => {
      this.paginator!.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    this.nifiService.find(
      this.qForms.appendPagingToFilter(null!, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator!.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort!.active,
      this.sort!.start);
  }

}
