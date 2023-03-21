import {Component, OnInit} from "@angular/core";
import {Log} from "ng2-logger/browser";
import {BaseComponent} from "./shared/components/base-component";
import {AppConstants} from "./app.constants";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {SecurityService} from "./security/security.service";
import {UtilityService} from "./shared/services/utility.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"]
})
export class AppComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;
  // Logger.
  private log = Log.create("AppComponent");
  // tslint:disable-next-line:variable-name
  private _isLoggedIn = false;

  constructor(private oidcService: OidcSecurityService,
    private securityUsersService: SecurityService, private utilityService: UtilityService) {
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
      // If the user is authenticated, get user permissions.
      if (isAuthenticated) {
        this.log.data(userData);
        this.securityUsersService.saveUserData(userData);
        this.securityUsersService.fetchPermissions().subscribe({
          next: permissions => {
            this.log.data(JSON.stringify(permissions));
            this.securityUsersService.savePermissions(permissions);
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not get user permissions.", err);
          }
        });
      }
    });
  }
}
