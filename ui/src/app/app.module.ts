/* tslint:disable:max-line-length */
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppComponent} from "./app.component";
import {routing} from "./app.routes";
import {QFormsModule} from "@qlack/forms";
import {NgProgressModule} from "ngx-progressbar";
import {NgProgressHttpModule} from "ngx-progressbar/http";
import {AuthInterceptor, AuthModule, LogLevel} from "angular-auth-oidc-client";
import {LayoutModule} from "./layout/layout.module";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatIconModule} from "@angular/material/icon";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatButtonModule} from "@angular/material/button";
import {FaConfig, FaIconLibrary, FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {
  faBars,
  faBarsProgress,
  faBell,
  faBroadcastTower,
  faCalendar,
  faCertificate,
  faChevronDown,
  faCircle,
  faCircleInfo,
  faCircleUp,
  faClipboard,
  faCubes,
  faDashboard,
  faDatabase,
  faDownload,
  faEnvelope,
  faFlag,
  faForward,
  faGlobe,
  faHeart,
  faHeartCircleBolt,
  faHome,
  faLayerGroup,
  faListCheck,
  faMemory,
  faMicrochip,
  faPause,
  faPercent,
  faPlay,
  faPlus,
  faRefresh,
  faSearch,
  faStamp,
  faStop,
  faStopwatch,
  faSun,
  faSwatchbook,
  faTag,
  faTerminal,
  faTrashCan,
  faTriangleExclamation,
  faUpRightFromSquare,
  faUser,
  faUserClock,
  faXmark
} from "@fortawesome/free-solid-svg-icons";
import {faGithub, faInstagram, faTwitter} from "@fortawesome/free-brands-svg-icons";
import {ComponentsModule} from "./shared/components/components.module";

@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    routing,
    HttpClientModule,
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
    FontAwesomeModule,
    ComponentsModule,
  ],
  exports: [],
  providers: [
    QFormsModule,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ]
})

export class AppModule {
  constructor(library: FaIconLibrary, faConfig: FaConfig) {
    library.addIcons(faSearch, faBell, faUser, faRefresh, faSun, faMicrochip, faHome, faBars,
      faHeart, faInstagram, faTwitter, faGithub, faMemory, faDatabase, faTerminal, faChevronDown,
      faSwatchbook, faUpRightFromSquare, faBroadcastTower, faClipboard, faDownload, faTrashCan,
      faXmark, faCubes, faTriangleExclamation, faGlobe, faCircleUp, faCircle, faPlay, faPause,
      faUserClock, faPlus, faLayerGroup, faTag, faCalendar, faListCheck, faFlag, faStop, faForward,
      faCircleInfo, faHeartCircleBolt, faEnvelope, faStopwatch, faBarsProgress, faDashboard,
      faPercent, faCertificate, faStamp);
    faConfig.fixedWidth = true;
  }
}
