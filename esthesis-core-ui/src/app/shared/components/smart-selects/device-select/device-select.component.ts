import {Component, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {BehaviorSubject, map, Observable, of, tap} from "rxjs";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs/operators";
import {DeviceDto} from "../../../../devices/dto/device-dto";
import {DevicesService} from "../../../../devices/devices.service";

@Component({
  selector: 'app-device-select',
  templateUrl: './device-select.component.html'
})
export class DeviceSelectComponent implements OnInit, OnChanges {
  @Input() parentForm!: FormGroup; // The parent form passed in.
  @Input() parentFormDeviceField!: string; // The name of the device field in the parent form.
  @Input() parentFormInitWithDevice: string[] = []; // Initially selected device ID.
  @Input() label: string = "Device"; // The label of the input field.
  @Input() bindValue: string = "id"; // The field to bind the value of the selected items.
  @Input() hint?: string; // The hint of the input field.
  items: DeviceDto[] = [];
  selectedItems: DeviceDto[] = [];
  currentPage = 0;
  pageSize = 20;
  hasMore = true;
  isLoading = false;
  searchInput$ = new BehaviorSubject<string>("");

  constructor(private readonly deviceService: DevicesService) {
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
    if (changes["parentFormInitWithDevice"]) {
      const changedDevice = changes["parentFormInitWithDevice"].currentValue;
      if (changedDevice && changedDevice.length > 0) {
        this.fetchSelectedItems(changedDevice);
      }
    }
  }

  fetchItems(term: string, page: number): Observable<DeviceDto[]> {
    if (!term) return of([]);

    this.isLoading = true;
    return this.deviceService.find(`hardwareId*=${term}&page=${page}&size=${this.pageSize}&sort=name,asc`)
    .pipe(
      tap(items => {
        this.isLoading = false;
        if (items.content.length < this.pageSize) this.hasMore = false;
      }),
      map(items => items.content)
    )
  }

  fetchSelectedItems(device: string[]) {
    return this.deviceService.findById(device).subscribe(items => {
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

