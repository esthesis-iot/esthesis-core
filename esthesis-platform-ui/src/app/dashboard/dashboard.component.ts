import {AfterViewInit, Component, OnInit, Renderer2} from '@angular/core';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent extends BaseComponent implements OnInit, AfterViewInit {

  constructor() {
    super();
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
  }

}
