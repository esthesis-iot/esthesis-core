import { Component} from '@angular/core';
import {BaseComponent} from '../shared/base-component';
import {AppConstants} from '../app.constants';
import {UserService} from '../users/user.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent extends BaseComponent {
  // The user email extracted from JWT.
  public userEmail: string;

  constructor(private userService: UserService) {
    super();
  }

  getUserEmail(): string {
    return this.userService.getJWTClaim(AppConstants.jwt.claims.USERNAME);
  }

}
