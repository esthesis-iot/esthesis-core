import {Component, OnInit, ViewEncapsulation, Renderer2} from '@angular/core';
import {Router} from '@angular/router';
import {Log} from 'ng2-logger/browser';
import {BaseComponent} from './shared/base-component';
import {JwtHelperService} from '@auth0/angular-jwt';
import {MatDialog} from '@angular/material';
import {UserService} from './users/user.service';
import {WebSocketService} from './services/web-socket.service';
import {OkCancelModalComponent} from './shared/display/ok-cancel-modal/ok-cancel-modal.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent extends BaseComponent implements OnInit {
  // Logger.
  private log = Log.create('AppComponent');

  // Controller for sidebar's visibility.
  sidebarVisibility = true;

  constructor(private userService: UserService, private router: Router, private jwtService: JwtHelperService,
              private wsService: WebSocketService, private dialog: MatDialog) {
    super();

    // Check if an expired JWT exists and remove it.
    const jwtString = localStorage.getItem(this.constants.JWT_STORAGE_NAME);
    if (jwtString) {
      try {
        if (this.jwtService.isTokenExpired(jwtString)) {
          this.log.data('Removing expired JWT.');
          localStorage.removeItem(this.constants.JWT_STORAGE_NAME);
        }
      } catch (err) {
        this.log.data('Could not decode JWT, removing it.');
        localStorage.removeItem(this.constants.JWT_STORAGE_NAME);
      }
    } else {
      this.log.data('Did not find a JWT.');
    }
  }

  ngOnInit() {
    this.log.info('Initialising application.');
    if (!this.isLoggedIn()) {
      this.router.navigate(['login']);
    } else {
      this.wsService.connect();

      // Check for token expiration.
      const checkInterval: any = setInterval(() => {
        let tokenExpired = false;
        const jwtString = localStorage.getItem(this.constants.JWT_STORAGE_NAME);
        if (jwtString) {
          try {
            tokenExpired = this.jwtService.isTokenExpired(jwtString);
          } catch (err) {
            // Ignore error, user does not have a JWT.
          }
        }

        if (tokenExpired) {
          this.wsService.disconnect();
          // Setup a timer to check for token expiration.
          window.clearInterval(checkInterval);
          this.dialog.open(OkCancelModalComponent, {
            disableClose: true,
            data: {
              title: 'Session expired',
              question: 'Your session has expired. Reload the page to refresh it.',
              buttons: {
                ok: false,
                cancel: false,
                reload: true
              }
            }
          });
        }
      }, 1000);
    }
  }

  toggleSidebar() {
    this.sidebarVisibility = !this.sidebarVisibility;
  }
}
