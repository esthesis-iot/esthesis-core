/* tslint:disable:max-line-length */
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppComponent} from "./app.component";
import {routing} from "./app.routes";
import {DisplayModule} from "./shared/component/display/display.module";
import {QFormsModule} from "@qlack/forms";
import {NgProgressModule} from "ngx-progressbar";
import {NgProgressHttpModule} from "ngx-progressbar/http";
import {AuthInterceptor, AuthModule, LogLevel} from "angular-auth-oidc-client";
import {LayoutModule} from "./layout/layout.module";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatIconModule} from "@angular/material/icon";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatButtonModule} from "@angular/material/button";
import {
  MAT_FORM_FIELD_DEFAULT_OPTIONS,
  MatFormFieldDefaultOptions
} from "@angular/material/form-field";
import {FaConfig, FaIconLibrary, FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {
  faBars,
  faBell,
  faHeart,
  faHome,
  faMicrochip,
  faRefresh,
  faSearch,
  faSun,
  faUser
} from "@fortawesome/free-solid-svg-icons";
import {BreadcrumbComponent} from "./shared/component/breadcrumb/breadcrumb.component";
import {faGithub, faInstagram, faTwitter} from "@fortawesome/free-brands-svg-icons";

const appearance: MatFormFieldDefaultOptions = {
  appearance: "outline",
  // hideRequiredMarker: false,
  // floatLabel: "always",
  // subscriptSizing: "fixed"
};

@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    AppComponent,
    BreadcrumbComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    routing,
    HttpClientModule,
    DisplayModule,
    LayoutModule,
    HttpClientModule,
    NgProgressModule.withConfig({
      color: "#50A7D7",
      debounceTime: 500,
      meteor: false,
      spinner: false,
      thick: true,
      trickleSpeed: 500
    }),
    NgProgressHttpModule,
    AuthModule.forRoot({
      config: {
        // TODO make this configurable
        authority: "http://esthesis-dev-keycloak/realms/esthesis",
        redirectUrl: window.location.origin,
        postLogoutRedirectUri: window.location.origin,
        clientId: "esthesis",
        scope: "openid profile offline_access",
        responseType: "code",
        silentRenew: true,
        useRefreshToken: true,
        renewTimeBeforeTokenExpiresInSeconds: 30,
        maxIdTokenIatOffsetAllowedInSeconds: 10,
        ignoreNonceAfterRefresh: true,
        secureRoutes: ["/dev", "/api"],
        logLevel: LogLevel.Warn
      }
    }),
    MatToolbarModule,
    MatIconModule,
    MatSnackBarModule,
    MatButtonModule,
    FontAwesomeModule
  ],
  providers: [
    {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: appearance},
    QFormsModule,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ]
})

export class AppModule {
  constructor(library: FaIconLibrary, faConfig: FaConfig) {
    library.addIcons(faSearch, faBell, faUser, faRefresh, faSun, faMicrochip, faHome, faBars,
      faHeart, faInstagram, faTwitter, faGithub);
    faConfig.fixedWidth = true;
  }
}
