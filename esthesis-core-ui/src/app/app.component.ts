import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "./shared/components/base-component";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {SecurityService} from "./security/security.service";
import {UtilityService} from "./shared/services/utility.service";
import {ChatbotService} from "./chatbot/chatbot.service";
import {AppConstants} from "./app.constants";
import {SettingDto} from "./settings/dto/setting-dto";
import {SettingsService} from "./settings/settings.service";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html"
})
export class AppComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  private _isLoggedIn = false;

  constructor(private oidcService: OidcSecurityService, private chatbotService: ChatbotService,
              private securityUsersService: SecurityService, private utilityService: UtilityService,
              private settingsService: SettingsService) {
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
        this.settingsService.findByNames([AppConstants.NAMED_SETTING.CHATBOT_ENABLED])
        .subscribe({
          next: (settings: SettingDto[]) => {
            if(settings[0].value === "true") {
              this.chatbotService.connect(true);
            }
          }, error: (onError: any) => {
            this.utilityService.popupErrorWithTraceId("Could not fetch settings.", onError);
          }
        });

      }
    });
  }
}
