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
import {TextModalComponent} from "../shared/component/display/text-modal/text-modal.component";
import {CommandService} from "./command.service";
import {CommandReplyDto} from "../dto/command-reply-dto";
import {CommandCreateComponent} from "./command-create.component";
import {QFormsService} from "@qlack/forms";
import {AppConstants} from "../app.constants";

@Component({
  selector: "app-command",
  templateUrl: "./command.component.html",
  styleUrls: ["./command.component.scss"]
})
export class CommandComponent extends BaseComponent implements OnInit, AfterViewInit {
  displayedColumns = ["command", "description", "hardwareId", "createdOn", "actions"];
  dataSource: MatTableDataSource<CommandRequestDto> = new MatTableDataSource<CommandRequestDto>();
  filterForm: FormGroup;
  // Expose application constants.
  constants = AppConstants;

  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;

  constructor(private fb: FormBuilder, private router: Router,
    private commandService: CommandService, private dialog: MatDialog,
    private qForms: QFormsService) {
    super();
    this.filterForm = this.fb.group({
      operation: ["", null],
    });
  }

  private formatPayload(commandReply: CommandReplyDto): string {
    let retVal = commandReply.payload;
    if (commandReply.payloadEncoding === "base64") {
      retVal = atob(commandReply.payload);
    }

    return retVal;
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

  download(replyId: number) {
    // TODO #62
  }

  view(requestId: number) {
    this.commandService.getReply(requestId).subscribe(onNext => {
      this.dialog.open(TextModalComponent, {
        data: {
          title: "Command output",
          text: this.formatPayload(onNext)
        }
      });
    });
  }

  refreshCurrentData() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.direction);
  }
}
