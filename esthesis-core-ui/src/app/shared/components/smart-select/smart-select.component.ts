import {Component, Input, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {BehaviorSubject, map, Observable, of, tap} from "rxjs";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";
import {QPageableReply} from "@qlack/forms";

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

  // The name of the field that will be updated in the parent form.
  @Input() parentFormField!: string;

  // The label of the select field that will be generated.
  @Input() label!: string;

  // The service to use for searches and lookups.
  @Input() service!: any;

  // Whether to allow multiple selection.
  @Input() multiple: boolean = true;

  // A hint to display in the select field.
  @Input() hint?: string;

  // The items loaded into the virtual scrolling area of the select field.
  items: any[] = [];

  // The name of the search method and term to use when searching data for the select options.
  @Input() searchMethod!: string;
  @Input() searchTerm!: string;
  // The name of the get method and term used when looking up specific item(s).
  @Input() getMethod!: string;
  // The DTO attribute to bind to the parent form.
  @Input() dtoValue!: string;
  // The DTO attribute to use for the label of the field (i.e. the value displayed)
  @Input() dtoLabel!: string;

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

    this.parentForm.get(this.parentFormField)?.valueChanges.subscribe(
      (items: string[]) => {
        if (items && items.length > 0) {
          this.fetchSelectedItems(items);
        }
      });
  }

  fetchItems(term: string, page: number): Observable<any[]> {
    if (!term) return of([]);

    this.isLoading = true;
    return this.service[`${this.searchMethod}`](
      `${this.searchTerm}*=${term}&page=${page}&size=${this.pageSize}&sort=name,asc`)
    .pipe(
      tap((items: QPageableReply<any>) => {
        this.isLoading = false;
        if (items.content.length < this.pageSize) this.hasMore = false;
      }),
      map((items: QPageableReply<any>) => items.content)
    )
  }

  fetchSelectedItems(selectedItems: string[]): void {
    this.service[`${this.getMethod}`](selectedItems).subscribe((items: any) => {
      if (!Array.isArray(items)) {
        items = [items];
      }
      this.initItems = items;
      this.items = [...this.initItems, ...this.items];
    });
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
