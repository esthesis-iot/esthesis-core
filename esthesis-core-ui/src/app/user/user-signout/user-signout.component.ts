import {Component} from "@angular/core";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {Router} from "@angular/router";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-user-signout",
  templateUrl: "./user-signout.component.html"
})
export class UserSignoutComponent {

  constructor(private oidcSecurityService: OidcSecurityService, private router: Router,
    private utilityService: UtilityService) {
    this.oidcSecurityService.logoffAndRevokeTokens().subscribe({
      next: () => {
        this.router.navigate(["dashboard"]);
      },
      error: (err) => {
        this.utilityService.popupError("An error occurred while signing out: " + err);
        this.router.navigate(["dashboard"]);
      }
    });
  }
}
