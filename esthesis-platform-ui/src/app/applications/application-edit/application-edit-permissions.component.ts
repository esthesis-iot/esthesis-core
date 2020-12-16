import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-application-edit-permissions',
  templateUrl: './application-edit-permissions.component.html',
  styleUrls: []
})
export class ApplicationEditPermissionsComponent implements OnInit {
  @Input() id!: number;

  constructor() {
  }

  ngOnInit() {
  }

}
