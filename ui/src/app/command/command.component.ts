import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTableDataSource} from "@angular/material/table";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {BaseComponent} from "../shared/component/base-component";
import {CommandRequestDto} from "../dto/command-request-dto";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {CommandService} from "./command.service";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../app.constants";
import {CommandCreateComponent} from "./command-create/command-create.component";
import {UtilityService} from "../shared/service/utility.service";
import {
  OkCancelModalComponent
} from "../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";

@Component({
  selector: "app-command",
  templateUrl: "./command.component.html",
  styleUrls: ["./command.component.scss"]
})
export class CommandComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["command", "description", "createdOn", "executedOn", "pills"];
  dataSource: MatTableDataSource<CommandRequestDto> = new MatTableDataSource<CommandRequestDto>();
  filterForm: FormGroup;
  // Expose application constants.
  constants = AppConstants;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
    private utilityService: UtilityService,
    private commandService: CommandService, private dialog: MatDialog,
    private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      command: ["", null],
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(onNext => {
      this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
        this.sort.start);
    });
  }

  ngAfterViewInit(): void {

    // Initial fetch of data.
    this.fetchData(0, this.paginator.pageSize, this.sort.active, this.sort.start);

    // Each time the sorting changes, reset the page number.
    this.sort.sortChange.subscribe((onNext: { active: string; direction: string; }) => {
      this.paginator.pageIndex = 0;
      this.fetchData(0, this.paginator.pageSize, onNext.active, onNext.direction);
    });
  }

  fetchData(page: number, size: number, sort: string, sortDirection: string) {
    // Convert FormGroup to a query string to pass as a filter.
    this.commandService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      null!, false, page, size, sort, sortDirection))
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
    this.dialog.open(CommandCreateComponent, {
      width: "40%",
    });
  }

  refreshCurrentData() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.direction);
  }

  purgeAll() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Purge",
        question: "Do you really want to purge all commands and replies?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.purgeAll().subscribe({
          next: () => {
            this.utilityService.popupSuccess("All commands and replies have been purged.");
            this.refreshCurrentData();
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not purge.", error);
          }
        });
      }
    });
  }

  purgeKeppLastDay() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Purge",
        question: "Do you really want to purge all commands and replies older than 1 day?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.purge(1).subscribe({
          next: () => {
            this.utilityService.popupSuccess("All commands and replies older than 1 day have been purged.");
            this.refreshCurrentData();
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not purge.", error);
          }
        });
      }
    });
  }

  purgeKeppLastWeek() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Purge",
        question: "Do you really want to purge all commands and replies older than 1 week?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.purge(7).subscribe({
          next: () => {
            this.utilityService.popupSuccess("All commands and replies older than 1 week have been purged.");
            this.refreshCurrentData();
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not purge.", error);
          }
        });
      }
    });
  }

  purgeKeppLastMonth() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Purge",
        question: "Do you really want to purge all commands and replies older than 1 month?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.purge(30).subscribe({
          next: () => {
            this.utilityService.popupSuccess("All commands and replies older than 1 month have been purged.");
            this.refreshCurrentData();
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not purge.", error);
          }
        });
      }
    });
  }
}
