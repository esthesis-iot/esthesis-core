import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-info-box-icon-value-legend',
  templateUrl: './info-box-icon-value-legend.component.html',
  styleUrls: ['./info-box-icon-value-legend.component.scss']
})
export class InfoBoxIconValueLegendComponent implements OnInit {

  @Input()
  color: string | undefined;

  @Input()
  icon: string | undefined;

  @Input()
  measurement: string | undefined;

  @Input()
  legend: string | undefined;

  constructor() { }

  ngOnInit() {
  }

}
