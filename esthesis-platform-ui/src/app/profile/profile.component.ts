import {Component} from '@angular/core';
import {BaseComponent} from '../shared/base-component';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent extends BaseComponent {

  constructor() {
    super();
  }
}
