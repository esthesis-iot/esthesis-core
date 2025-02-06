import {Component, Input, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {BehaviorSubject, map, Observable, of, tap} from "rxjs";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";
import {QPageableReply} from "@qlack/forms";

// What type of DTO value should be bound to the parent form.
export enum SMART_SELECT_BIND_VALUE {
  NAME = "name",
  ID = "id",
  HARDWARE_ID = "hardwareId"
}

// Parent class for all smart select components. Smart select components provide select input
// elements supporting server-side filtering and virtual scrolling, so that very large datasets
// can be loaded and displayed efficiently.
@Component({
  selector: "app-smart-select",
  templateUrl: "./smart-select.component.html"
})
export class SmartSelectComponent implements OnInit {
  // The parent form in which updates will be made.
  @Input() parentForm!: FormGroup;
  // The name of the data field that will be updated in the parent form.
  @Input() parentFormTargetField!: string;
  // The label of the select field that will be generated.
  @Input() label!: string;
  // The service to use for searches and lookups. This service should implement search-by-id and,
  // optionally, search-by-name.
  @Input() service!: any;
  // The DTO value to bind to the parent form.
  @Input() bindValue: SMART_SELECT_BIND_VALUE = SMART_SELECT_BIND_VALUE.ID;
  // The DTO value to use for the label of the field (i.e. the value displayed)
  @Input() bindLabel: string = "name";
  // Whether to allow multiple selection.
  @Input() multiple: boolean = true;
  // A hint to display in the select field.
  @Input() hint?: string;
  // The items loaded into the virtual scrolling area of the select field.
  items: any[] = [];
  // Items originally selected when the component was initialized.
  initItems: any[] = [];
  currentPage = 0;
  pageSize = 20;
  hasMore = true;
  isLoading = false;
  searchInput$ = new BehaviorSubject<string>("");

  constructor() {
  }

  ngOnInit(): void {

    this.searchInput$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => this.resetPagination()),
      switchMap(term => this.fetchItems(term, this.currentPage))
    ).subscribe(items => {
      this.items = items;
      this.mergeSelectedItems();
    });

    this.parentForm.get(this.parentFormTargetField)?.valueChanges.subscribe(
      (items: string[]) => {
        if (items && items.length > 0) {
          this.fetchSelectedItems(items);
        }
      });
  }

  fetchItems(term: string, page: number): Observable<any[]> {
    if (!term) return of([]);

    this.isLoading = true;
    let searchQueryTerm;
    if (this.bindValue === SMART_SELECT_BIND_VALUE.NAME || this.bindValue === SMART_SELECT_BIND_VALUE.ID) {
      searchQueryTerm = "name";
    } else if (this.bindValue === SMART_SELECT_BIND_VALUE.HARDWARE_ID) {
      searchQueryTerm = "hardwareId";
    }else {
      throw new Error("Unknown bindValue.");
    }

    return this.service["find"](
      `${searchQueryTerm}*=${term}&page=${page}&size=${this.pageSize}&sort=name,asc`)
    .pipe(
      tap((items: QPageableReply<any>) => {
        this.isLoading = false;
        if (items.content.length < this.pageSize) this.hasMore = false;
      }),
      map((items: QPageableReply<any>) => items.content)
    )
  }

  fetchSelectedItems(selectedItems: string[]): void {
    if (this.bindValue === SMART_SELECT_BIND_VALUE.NAME) {
      this.service["findByNames"](selectedItems).subscribe((items: any) => {
        this.initItems = items;
        this.items = [...this.initItems, ...this.items];
      });
    } else if (this.bindValue === SMART_SELECT_BIND_VALUE.ID) {
      this.service["findByIds"](selectedItems).subscribe((items: any) => {
        this.initItems = items;
        this.items = [...this.initItems, ...this.items];
      });
    } else if (this.bindValue === SMART_SELECT_BIND_VALUE.HARDWARE_ID) {
      this.service["findDeviceByPartialHardwareId"](selectedItems).subscribe((items: any) => {
        this.initItems = items;
        this.items = [...this.initItems, ...this.items];
      });
    } else {
      throw new Error("Unknown bindValue.");
    }
  }

  onScrollToEnd() {
    if (this.isLoading || !this.hasMore) return;

    this.currentPage++;
    this.fetchItems(this.searchInput$.value, this.currentPage).subscribe(
      newItems => {
        this.items.push(...newItems);
        this.mergeSelectedItems();
      }
    );
  }

  resetPagination() {
    this.currentPage = 0;
    this.hasMore = true;
    this.items = [...this.initItems];
  }

  mergeSelectedItems() {
    const selectedIds = new Set(this.initItems.map(item => item.id));
    this.items = [...this.initItems, ...this.items.filter(item => !selectedIds.has(item.id))];
  }

}
