import {AfterViewInit, Component, OnInit} from '@angular/core';
import {RxStompService} from '@stomp/ng2-stompjs';
import {BaseComponent} from '../shared/base-component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent extends BaseComponent implements OnInit, AfterViewInit {

  constructor(private rxStompService: RxStompService) {
    super();
  }

  ngOnInit() {

  }

  ngAfterViewInit(): void {
  }

}
