import {AfterViewInit, Component, Input, OnInit, Optional, ViewChild} from "@angular/core";
import {MatSort} from "@angular/material/sort";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {TagDto} from "../dto/tag-dto";
import {TagsService} from "../tags.service";
import {QFormsService} from "@qlack/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {BaseComponent} from "../../shared/components/base-component";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {MatDialogRef} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-tags-list",
  templateUrl: "./tags-list.component.html"
})
export class TagsListComponent extends SecurityBaseComponent implements OnInit, AfterViewInit {
  // References to sorting and pagination.
  @ViewChild(MatSort, {static: true}) sort!: MatSort;
  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @Input() embedded = false;

  displayedColumns = ["name"];
  dataSource: MatTableDataSource<TagDto> = new MatTableDataSource<TagDto>();
  filterForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router, private tagService: TagsService,
    private qForms: QFormsService, @Optional() private dialogRef: MatDialogRef<TagsListComponent>,
    private utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.TAG);
    this.filterForm = this.fb.group({
      name: [],
    });
  }

  ngOnInit() {
    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe({
      next: () => {
        this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
          this.sort.start);
      }
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
    this.tagService.find(this.qForms.makeQueryStringForData(this.filterForm.getRawValue(),
      [], false, page, size, sort, sortDirection)).subscribe({
      next: (onNext) => {
        this.dataSource.data = onNext.content;
        this.paginator.length = onNext.totalElements;
      }, error: (onError: any) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch tags.", onError);
      }
    });
  }

  clearFilter() {
    this.filterForm.reset();
  }

  changePage() {
    this.fetchData(this.paginator.pageIndex, this.paginator.pageSize, this.sort.active,
      this.sort.start);
  }

  embeddedClick(tag: TagDto) {
    this.dialogRef.close(tag);
  }
}
