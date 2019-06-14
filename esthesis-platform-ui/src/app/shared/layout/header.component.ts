import { Component} from '@angular/core';
import {BaseComponent} from '../component/base-component';
import {UserService} from '../../users/user.service';
import {AppConstants} from '../../app.constants';

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
