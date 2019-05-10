import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {BaseComponent} from '../shared/base-component';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {QFormsService} from '@eurodyn/forms';
import {ProvisioningService} from './provisioning.service';
import {ProvisioningDto} from '../dto/provisioning-dto';
import {TagService} from '../tags/tag.service';
import {TagDto} from '../dto/tag-dto';
import * as _ from 'lodash';

@Component({
  selector: 'app-provisioning',
  templateUrl: './provisioning.component.html',
  styleUrls: ['./provisioning.component.scss']
})
export class ProvisioningComponent extends BaseComponent implements OnInit, AfterViewInit {
  columns = ['name', 'createdOn', 'tags', 'version', 'defaultIP', 'size', 'status'];
  datasource = new MatTableDataSource<ProvisioningDto>();
  availableTags: TagDto[];

  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private provisioningService: ProvisioningService, private qForms: QFormsService,
              private tagService: TagService) {
    super();
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Get available tags.
    this.tagService.getAll().subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Each time the sorting changes, reset the page number.
    this.sort.sortChange.subscribe(onNext => {
      this.paginator.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    this.provisioningService.getAll(
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

  resolveTag(id: number) {
    return _.find(this.availableTags['content'], {id: id});
  }
}

