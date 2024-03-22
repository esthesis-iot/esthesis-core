import {Component} from "@angular/core";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user-signout',
  templateUrl: './user-signout.component.html'
})
export class UserSignoutComponent {

  constructor(private oidcSecurityService: OidcSecurityService, private router: Router) {
    oidcSecurityService.logoffLocal();
    this.router.navigate(["dashboard"]);
  }
}
