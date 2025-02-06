import {Component, Input, OnInit} from "@angular/core";
import {TagDto} from "../../../../tags/dto/tag-dto";
import {BehaviorSubject, map, Observable, of, tap} from "rxjs";
import {TagsService} from "../../../../tags/tags.service";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";
import {FormGroup} from "@angular/forms";

export enum TAG_SELECT_BIND_VALUE {
  NAME = "name",
  ID = "id"
}

@Component({
  selector: "app-tag-select",
  templateUrl: "./tag-select.component.html"
})
export class TagSelectComponent implements OnInit {
  @Input() parentForm!: FormGroup; // The parent form passed in.
  @Input() parentFormTagsField!: string; // The name of tags field in the parent form.
  @Input() label: string = "Tags"; // The label of the input field.
  @Input() bindValue: TAG_SELECT_BIND_VALUE = TAG_SELECT_BIND_VALUE.ID; // The TagDto field to set as value of the selected tag.
  @Input() multiple: boolean = true; // Whether to allow multiple selection.
  items: TagDto[] = [];
  selectedItems: TagDto[] = [];
  currentPage = 0;
  pageSize = 20;
  hasMore = true;
  isLoading = false;
  searchInput$ = new BehaviorSubject<string>("");

  constructor(private readonly tagsService: TagsService) {
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

    this.parentForm.get(this.parentFormTagsField)?.valueChanges.subscribe(
      (tags: string[]) => {
        if (tags && tags.length > 0) {
          this.fetchSelectedItems(tags);
        }
      });
  }

  fetchItems(term: string, page: number): Observable<TagDto[]> {
    if (!term) return of([]);

    this.isLoading = true;
    return this.tagsService.find(`name*=${term}&page=${page}&size=${this.pageSize}&sort=name,asc`)
    .pipe(
      tap(items => {
        this.isLoading = false;
        if (items.content.length < this.pageSize) this.hasMore = false;
      }),
      map(items => items.content)
    )
  }

  fetchSelectedItems(tags: string[]): void {
    if (this.bindValue === TAG_SELECT_BIND_VALUE.NAME) {
      this.tagsService.findByNames(tags).subscribe(items => {
        this.selectedItems = items;
        this.items = [...this.selectedItems, ...this.items];
      });
    } else if (this.bindValue === TAG_SELECT_BIND_VALUE.ID) {
      this.tagsService.findByIds(tags).subscribe(items => {
        this.selectedItems = items;
        this.items = [...this.selectedItems, ...this.items];
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
    this.items = [...this.selectedItems];
  }

  mergeSelectedItems() {
    const selectedIds = new Set(this.selectedItems.map(item => item.id));
    this.items = [...this.selectedItems, ...this.items.filter(item => !selectedIds.has(item.id))];
  }

}
