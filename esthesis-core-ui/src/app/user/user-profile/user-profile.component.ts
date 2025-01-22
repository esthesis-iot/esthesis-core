import {Component} from "@angular/core";
import {OidcSecurityService, UserDataResult} from "angular-auth-oidc-client";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html'
})
export class UserProfileComponent {
  form!: FormGroup;

  constructor(private readonly oidcSecurityService: OidcSecurityService,
    private readonly fb: FormBuilder) {
  }

  ngOnInit() {
    // Set up the form.
    this.oidcSecurityService.userData$.subscribe((userData: UserDataResult) => {
      this.form = this.fb.group({
        emailVerified: [{value: userData.userData.email_verified, disabled: true}],
        familyName: [{value: userData.userData.family_name, disabled: true}],
        givenName: [{value: userData.userData.given_name, disabled: true}],
        name: [{value: userData.userData.name, disabled: true}],
        preferredUsername: [{value: userData.userData.preferred_username, disabled: true}],
        sub: [{value: userData.userData.sub, disabled: true}],
        email: [{value: userData.userData.email, disabled: true}],
      });
    });
  }
}
