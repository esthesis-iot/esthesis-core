import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {icon, latLng, marker, tileLayer} from 'leaflet';
import {MatAutocompleteSelectedEvent, MatChipInputEvent} from '@angular/material';
import {FormControl} from '@angular/forms';
import {map, startWith} from 'rxjs/operators';

@Component({
  selector: 'app-device',
  templateUrl: './device.component.html',
  styleUrls: ['./device.component.scss']
})
export class DeviceComponent implements OnInit {
  options = {
    layers: [
      tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 18})
    ],
    zoom: 14,
    center: latLng(47.2287109, 14.3009642)
  };

  layer1 = [
    marker([47.2287109, 14.3009642], {
      icon: icon({
        iconSize: [25, 41],
        iconAnchor: [13, 41],
        iconUrl: 'assets/marker-icon.png',
        shadowUrl: 'assets/marker-shadow.png'
      })
    }),
  ];

  groupCtrl = new FormControl();
  filteredGroups: Observable<string[]>;
  groups: string[] = ['Karmelitermarkt'];
  allGroups: string[] = ['Alte Donau', 'Vienna', 'Praterstern', 'Grinzing', 'Opernring'];

  @ViewChild('groupInput') groupInput: ElementRef<HTMLInputElement>;
  id: string;

  constructor(private route: ActivatedRoute) {
    this.filteredGroups = this.groupCtrl.valueChanges.pipe(
      startWith(null),
      map((group: string | null) => group ? this._filter(group) : this.allGroups.slice()));

  }

  ngOnInit() {
    this.id = this.route.snapshot.params.id;
  }

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      this.groups.push(value.trim());
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }

    this.groupCtrl.setValue(null);
  }

  remove(group: string): void {
    const index = this.groups.indexOf(group);

    if (index >= 0) {
      this.groups.splice(index, 1);
    }
  }

  selected(event: MatAutocompleteSelectedEvent): void {
    this.groups.push(event.option.viewValue);
    this.groupInput.nativeElement.value = '';
    this.groupCtrl.setValue(null);
  }

  private _filter(value: string): string[] {
    const filterValue = value.toLowerCase();

    return this.allGroups.filter(group => group.toLowerCase().indexOf(filterValue) === 0);
  }

}
