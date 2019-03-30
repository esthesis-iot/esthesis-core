import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-info-box-icon-value-legend',
  templateUrl: './info-box-icon-value-legend.component.html',
  styleUrls: ['./info-box-icon-value-legend.component.scss']
})
export class InfoBoxIconValueLegendComponent implements OnInit {

  @Input()
  color: string;

  @Input()
  icon: string;

  @Input()
  measurement: string;

  @Input()
  legend: string;

  constructor() { }

  ngOnInit() {
  }

}
