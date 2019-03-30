import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../shared/base-component';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {CaDto} from '../dto/ca-dto';
import {QFormsService} from '@eurodyn/forms';
import {CertificatesService} from './certificates.service';
import {CertificateDto} from '../dto/certificate-dto';

@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.scss']
})
export class CertificatesComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['cn', 'issued', 'validity', 'issuer'];
  datasource = new MatTableDataSource<CertificateDto>();
  parentCAs: CaDto[];

  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private certificateSerice: CertificatesService, private qForms: QFormsService) {
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
    this.certificateSerice.getAll(this.qForms.appendPagingToFilter(null, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.datasource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active, this.sort.start);
  }

}
