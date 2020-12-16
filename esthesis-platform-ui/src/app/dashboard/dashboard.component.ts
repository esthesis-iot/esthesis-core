import {AfterViewInit, Component, OnInit} from '@angular/core';
import {BaseComponent} from '../shared/component/base-component';
import {NiFiService} from '../infrastructure/infrastructure-nifi/nifi.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent extends BaseComponent implements OnInit, AfterViewInit {

  constructor(private nifiService: NiFiService) {
    super();
  }

  ngOnInit() {
    this.nifiService.getActive().subscribe(value => {
      value
        ? localStorage.setItem("activeNiFi", value.id!.toString())
        : localStorage.removeItem("activeNiFi")
    });
  }

  ngAfterViewInit(): void {
  }

}
