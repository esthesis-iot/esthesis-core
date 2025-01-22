import {Component, OnInit} from "@angular/core";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR} from "@angular/forms";
import * as solidIcons from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: "app-mat-icon-picker",
  templateUrl: "./mat-icon-picker.component.html",
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: MatIconPickerComponent
    }
  ]
})
export class MatIconPickerComponent implements OnInit, ControlValueAccessor {
  private readonly allIcons: string[] = [];
  filteredIcons: string[] = [];
  filterForm: FormGroup;
  selectedIcon?: string;
  // The state of the component (open/closed).
  state = false;
  // The state of the search button.
  searchButtonState = true;

  constructor(private readonly fb: FormBuilder) {
    this.filterForm = this.fb.group({
      icon: [],
    });

    for (let icon in solidIcons) {
      // Convert camelCase to kebab-case.
      const iconName = icon.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase();

      // Ignore icons.
      // Add icon if it doesn't end with a number, or with 'az' or 'za'.
      if (!iconName.endsWith("az") && !iconName.endsWith("za") && !RegExp(/\d$/).exec(iconName)) {
        this.allIcons.push(iconName);
      }
    }
  }

  onChange: (value: string) => void = () => {
  };

  onTouched: () => void = () => {
  };

  select(icon: string) {
    this.selectedIcon = icon;
    this.onChange(icon);
    this.state = false;
    this.filterForm.reset();
  }

  writeValue(obj: any): void {
    this.selectedIcon = obj;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.searchButtonState = isDisabled;
  }

  ngOnInit(): void {
    this.filterIcons();

    // Listen for filter changes to fetch new data.
    this.filterForm.valueChanges.pipe(
      debounceTime(250),
      distinctUntilChanged()
    ).subscribe(onNext => {
      this.filterIcons(onNext.icon);
    });
  }

  filterIcons(filter?: string) {
    this.filteredIcons = this.allIcons
    .filter(icon => !filter || icon.includes(filter));
  }

}
