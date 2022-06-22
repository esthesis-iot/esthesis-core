import {Component, OnInit} from '@angular/core';
import {Log} from 'ng2-logger/browser';
import {BaseComponent} from './shared/component/base-component';
import {AppConstants} from "./app.constants";
import {AuthenticatedResult, OidcSecurityService} from "angular-auth-oidc-client";
import {Observable} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent extends BaseComponent implements OnInit {
  // Logger.
  private log = Log.create('AppComponent');
  // Expose application constants.
  constants = AppConstants;

  // Controller for sidebar's visibility.
  sidebarVisibility = true;

  constructor(private oidcService: OidcSecurityService) {
    super();
  }

  ngOnInit() {
  }

  toggleSidebar() {
    this.sidebarVisibility = !this.sidebarVisibility;
  }

  isLoggedIn(): Observable<AuthenticatedResult> {
    return this.oidcService.isAuthenticated$;
  }
}
