import {Component, OnInit} from "@angular/core";
import {Log} from "ng2-logger/browser";
import {BaseComponent} from "./shared/components/base-component";
import {AppConstants} from "./app.constants";
import {OidcSecurityService} from "angular-auth-oidc-client";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"]
})
export class AppComponent extends BaseComponent implements OnInit {
  // Logger.
  private log = Log.create("AppComponent");
  // Expose application constants.
  constants = AppConstants;
  // tslint:disable-next-line:variable-name
  private _isLoggedIn = false;

  constructor(private oidcService: OidcSecurityService) {
    super();

    // Check if a specific theme has already been saved for this user.
    // tslint:disable-next-line:no-unused-expression
    localStorage.getItem("theme") && document.querySelector("html")!.setAttribute("data-theme", localStorage.getItem("theme")!);
  }

  isLoggedIn(): boolean {
    return this._isLoggedIn;
  }

  ngOnInit() {
    // This is a necessary call as per
    // https://angular-auth-oidc-client.com/docs/documentation/auto-login
    this.oidcService.checkAuth().subscribe(({isAuthenticated, userData, accessToken}) => {
      this._isLoggedIn = isAuthenticated;
    });
  }
}
