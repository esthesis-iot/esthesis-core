import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-application-edit-permissions',
  templateUrl: './application-edit-permissions.component.html',
  styleUrls: ['./application-edit-permissions.component.scss']
})
export class ApplicationEditPermissionsComponent implements OnInit {
  @Input() id: string;

  constructor() {
  }

  ngOnInit() {
  }

}
