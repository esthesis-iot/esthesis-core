import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Router} from '@angular/router';
import {QFormsService} from '@eurodyn/forms';
import {BaseComponent} from '../shared/component/base-component';
import {ControlService} from './control.service';
import {CommandRequestDto} from '../dto/command-request-dto';
import 'rxjs/add/operator/debounceTime';
import {CommandPopupService} from '../shared/component/commands/command-popup.service';

@Component({
  selector: 'app-control',
  templateUrl: './control.component.html',
  styleUrls: ['./control.component.scss']
})
export class ControlComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ['command', 'description', 'hardwareId', 'createdOn'];
  dataSource: MatTableDataSource<CommandRequestDto> = new MatTableDataSource<CommandRequestDto>();
  filterForm: FormGroup;

  // References to sorting and pagination.
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
              private controlService: ControlService,
              private qForms: QFormsService, private commandPopupService: CommandPopupService) {
    super();
    this.filterForm = this.fb.group({
      command: ['', null],
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.debounceTime(500).subscribe(onNext => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
        this.sort.start);
    });
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
    this.controlService.getAll(this.qForms.makeQueryString(this.filterForm,
      null, false, page, size, sort, sortDirection))
    .subscribe(onNext => {
      this.dataSource.data = onNext.content;
      this.paginator.length = onNext.totalElements;
    });
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  clearFilter() {
    this.filterForm.reset();
  }

  create() {
    this.commandPopupService.commandPopup();
  }
}
