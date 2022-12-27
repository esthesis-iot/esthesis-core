import {Component} from "@angular/core";
import {BaseComponent} from "../shared/component/base-component";
import {UserService} from "../users/user.service";
import {Observable} from "rxjs";
import {AuthenticatedResult, OidcSecurityService} from "angular-auth-oidc-client";

@Component({
  selector: "app-header",
  templateUrl: "./header.component.html",
  styleUrls: ["./header.component.scss"]
})
export class HeaderComponent extends BaseComponent {
  // The user email extracted from JWT.
  public userEmail: string | undefined;

  constructor(private userService: UserService, private oidcService: OidcSecurityService) {
    super();
  }

  getUserEmail(): string | null {
    // TODO fix
    // return this.userService.getJWTClaim(AppConstants.jwt.claims.EMAIL);
    return "TEST123";
  }

  isLoggedIn(): Observable<AuthenticatedResult> {
    return this.oidcService.isAuthenticated$;
  }
}
