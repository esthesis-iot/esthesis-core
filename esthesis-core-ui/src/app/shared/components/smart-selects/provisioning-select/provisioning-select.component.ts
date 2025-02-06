import {Component, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {BehaviorSubject, map, Observable, of, tap} from "rxjs";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";
import {ProvisioningService} from "../../../../provisioning/provisioning.service";
import {ProvisioningDto} from "../../../../provisioning/dto/provisioning-dto";

@Component({
  selector: 'app-provisioning-select',
  templateUrl: './provisioning-select.component.html'
})
export class ProvisioningSelectComponent implements OnInit, OnChanges {
  @Input() parentForm!: FormGroup; // The parent form passed in.
  @Input() parentFormProvisioningField!: string; // The name of provisioning field in the parent form.
  @Input() parentFormInitWithProvisioning: string[] = []; // Initially selected provisioning IDs.
  @Input() label: string = "Provisioning package"; // The label of the input field.
  @Input() bindValue: string = "id"; // The field to bind the value of the selected items.
  @Input() hint?: string; // The hint of the input field.
  items: ProvisioningDto[] = [];
  selectedItems: ProvisioningDto[] = [];
  currentPage = 0;
  pageSize = 20;
  hasMore = true;
  isLoading = false;
  searchInput$ = new BehaviorSubject<string>("");

  constructor(private readonly provisioningService: ProvisioningService) {
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
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes["parentFormInitWithProvisioning"]) {
      const changedProvisioning = changes["parentFormInitWithProvisioning"].currentValue;
      if (changedProvisioning && changedProvisioning.length > 0) {
        this.fetchSelectedItems(changedProvisioning);
      }
    }
  }

  fetchItems(term: string, page: number): Observable<ProvisioningDto[]> {
    if (!term) return of([]);

    this.isLoading = true;
    return this.provisioningService.find(`name*=${term}&page=${page}&size=${this.pageSize}&sort=name,asc&available=true`)
    .pipe(
      tap(items => {
        this.isLoading = false;
        if (items.content.length < this.pageSize) this.hasMore = false;
      }),
      map(items => items.content)
    )
  }

  fetchSelectedItems(provisioning: string[]) {
    return this.provisioningService.findById(provisioning).subscribe(items => {
      this.selectedItems = [items];
      this.items = [...this.selectedItems, ...this.items];
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
    this.items = [...this.selectedItems];
  }

  mergeSelectedItems() {
    const selectedIds = new Set(this.selectedItems.map(item => item.id));
    this.items = [...this.selectedItems, ...this.items.filter(item => !selectedIds.has(item.id))];
  }

}

