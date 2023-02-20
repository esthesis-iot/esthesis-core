import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {BaseComponent} from "../../shared/components/base-component";
import {CommandRequestDto} from "../dto/command-request-dto";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {CommandsService} from "../commands.service";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../../app.constants";
import {CommandCreateComponent} from "../command-create/command-create.component";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-commands-list",
  templateUrl: "./commands-list.component.html",
  styleUrls: ["./commands-list.component.scss"]
})
export class CommandsListComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["command", "createdOn", "dispatchedOn", "pills"];
  dataSource: MatTableDataSource<CommandRequestDto> = new MatTableDataSource<CommandRequestDto>();
  filterForm: FormGroup;
  // Expose application constants.
  constants = AppConstants;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
    private utilityService: UtilityService,
    private commandService: CommandsService, private dialog: MatDialog,
    private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      command: [],
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
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
    .subscribe({
      next: (response) => {
        this.dataSource.data = response.content;
        this.paginator.length = response.totalElements;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch commands.", error);
      }
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

  purgeKeep(keepDays: number) {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Purge",
        question: "Do you really want to purge all commands and replies older than " + keepDays +
          (keepDays === 1 ? "day?" : "days?"),
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.purge(1).subscribe({
          next: () => {
            this.utilityService.popupSuccess(
              "All commands and replies older than " + keepDays +
              (keepDays === 1 ? "day" : "days") + " have been purged.");
            this.refreshCurrentData();
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId(
              "There was an error trying to purge, please try again later.", error);
          }
        });
      }
    });
  }
}
