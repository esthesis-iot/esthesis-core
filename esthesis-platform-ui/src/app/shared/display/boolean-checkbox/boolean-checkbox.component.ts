import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-boolean-checkbox',
  templateUrl: './boolean-checkbox.component.html',
  styleUrls: ['./boolean-checkbox.component.scss']
})
export class BooleanCheckboxComponent implements OnInit {
  @Input() public value: boolean;

  constructor() { }

  ngOnInit() {
  }

}
