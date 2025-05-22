import {Component, OnInit} from "@angular/core";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {Router} from "@angular/router";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-user-signout",
  templateUrl: "./user-signout.component.html"
})
export class UserSignoutComponent implements OnInit {

  constructor(private readonly oidcSecurityService: OidcSecurityService,
              private readonly router: Router, private readonly utilityService: UtilityService,
              private oidcService: OidcSecurityService) {
  }

  ngOnInit(): void {
    this.oidcSecurityService.logoffAndRevokeTokens().subscribe({
      next: () => {
        this.oidcService.authorize();
      },
      error: (err) => {
        this.utilityService.popupError("An error occurred while signing out: " + err);
        this.router.navigate(["dashboard"]);
      }
    });
  }
}
