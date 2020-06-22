import {AfterViewInit, Component, OnInit, Renderer2} from '@angular/core';
import {BaseComponent} from '../shared/component/base-component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent extends BaseComponent implements OnInit, AfterViewInit {

  // constructor(private renderer: Renderer2) {
  constructor() {
    super();
  }

  ngOnInit() {
    //this.renderer.removeAttribute(document.body, 'style');
  }

  ngAfterViewInit(): void {
  }

}
