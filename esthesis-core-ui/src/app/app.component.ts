import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "./shared/components/base-component";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {SecurityService} from "./security/security.service";
import {UtilityService} from "./shared/services/utility.service";
import {HttpEvent, HttpHandlerFn, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html"
})
export class AppComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  private _isLoggedIn = false;

  constructor(private readonly oidcService: OidcSecurityService,
    private readonly securityUsersService: SecurityService,
    private readonly utilityService: UtilityService) {
    super();

    // Check if a specific theme has already been saved for this user.
    localStorage.getItem("theme") &&
      document.querySelector("html")!.setAttribute("data-theme", localStorage.getItem("theme")!);
  }

  isLoggedIn(): boolean {
    return this._isLoggedIn;
  }

  ngOnInit() {
    // https://angular-auth-oidc-client.com/docs/documentation/auto-login
    this.oidcService.checkAuth().subscribe((loginResponse) => {
      this._isLoggedIn = loginResponse.isAuthenticated;
      // If the user is authenticated, get user permissions.
      if (loginResponse.isAuthenticated) {
        this.securityUsersService.saveUserData(loginResponse.userData);
        this.securityUsersService.getPermissions().subscribe({
          next: () => {
            this.securityUsersService.authDone(true);
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not get user permissions.", err);
          }
        });
      }
    });
  }
}

export function loggingInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  console.log(req.url);
  return next(req);
}
