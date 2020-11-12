import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import { MatTabGroup } from '@angular/material/tabs';

@Component({
  selector: 'app-infrastructure',
  templateUrl: './infrastructure.component.html',
  styleUrls: []
})
export class InfrastructureComponent implements OnInit, AfterViewInit {
  @ViewChild(MatTabGroup, { static: true }) tabs: MatTabGroup;

  constructor(private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    // Tab activation.
    this.activatedRoute.fragment.subscribe((fragment: string) => {
      if (fragment === 'overview') {
        this.tabs.selectedIndex = 0;
      } else if (fragment === 'mqtt') {
        this.tabs.selectedIndex = 1;
      } else if (fragment === 'nifi') {
        this.tabs.selectedIndex = 2;
      }
    });
  }

}
