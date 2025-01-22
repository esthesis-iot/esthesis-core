import {
  HTTP_INTERCEPTORS,
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from "@angular/common/http";
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppComponent} from "./app.component";
import {routing} from "./app.routes";
import {QFormsModule} from "@qlack/forms";
import {AuthInterceptor, AuthModule, StsConfigLoader} from "angular-auth-oidc-client";
import {LayoutModule} from "./layout/layout.module";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatIconModule} from "@angular/material/icon";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatButtonModule} from "@angular/material/button";
import {FaConfig, FaIconLibrary, FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {
  faAddressCard,
  faArrowUpRightFromSquare,
  faBars,
  faBarsProgress,
  faBell,
  faBoxArchive,
  faBroadcastTower,
  faBug,
  faBuildingShield,
  faCalendar,
  faCaretDown,
  faCertificate,
  faCheck,
  faChevronDown,
  faCircle,
  faCircleInfo,
  faCirclePlay,
  faCircleUp,
  faCircleXmark,
  faClipboard,
  faCog,
  faCubes,
  faDashboard,
  faDatabase,
  faDesktop,
  faDiagramProject,
  faDownload,
  faEnvelope,
  faEnvelopeOpen,
  faEraser,
  faFileCircleCheck,
  faFileCirclePlus,
  faFlag,
  faForward,
  faGear,
  faGlobe,
  faHeart,
  faHeartCircleBolt,
  faHome,
  faIcons,
  faIdBadge,
  faLayerGroup,
  faListCheck,
  faMaximize,
  faMemory,
  faMicrochip,
  faMinusCircle,
  faNetworkWired,
  faPaste,
  faPause,
  faPercent,
  faPlay,
  faPlus,
  faRefresh,
  faRulerCombined,
  faSearch,
  faShareNodes,
  faShieldHalved,
  faSpinner,
  faSquare,
  faSquareCheck,
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
  faUsers,
  faUsersBetweenLines,
  faXmark,
  faXmarksLines
} from "@fortawesome/free-solid-svg-icons";
import {faGithub, faInstagram, faTwitter} from "@fortawesome/free-brands-svg-icons";
import {ComponentsModule} from "./shared/components/components.module";
import {CallbackComponent} from "./callback.component";
import {httpLoaderFactory} from "./shared/services/auth.service";
import {provideCharts, withDefaultRegisterables} from "ng2-charts";
import {NgProgressbar, provideNgProgressOptions} from "ngx-progressbar";
import {NgProgressHttp, progressInterceptor, provideNgProgressHttp} from "ngx-progressbar/http";
import {provideLumberjack} from "@ngworker/lumberjack";
import {provideLumberjackConsoleDriver} from "@ngworker/lumberjack/console-driver";

@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    AppComponent,
    CallbackComponent
  ],
  exports: [],
  imports: [BrowserModule,
    BrowserAnimationsModule,
    routing,
    LayoutModule,
    AuthModule.forRoot({
      loader: {
        provide: StsConfigLoader,
        useFactory: httpLoaderFactory,
        deps: [HttpClient],
      },
    }),
    MatToolbarModule,
    MatIconModule,
    MatSnackBarModule,
    MatButtonModule,
    FontAwesomeModule,
    ComponentsModule, NgProgressbar, NgProgressHttp],
  providers: [
    QFormsModule,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    provideCharts(withDefaultRegisterables()),
    provideHttpClient(withInterceptors([progressInterceptor])),
    provideNgProgressOptions({
      flat: false
    }),
    provideNgProgressHttp({
      silentApis: [
        "api/dashboard/v1/sub"
      ]
    }),
    provideLumberjack(),
    provideLumberjackConsoleDriver(),
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
      faPercent, faCertificate, faStamp, faShieldHalved, faDiagramProject, faCheck, faSquareCheck,
      faSquare, faNetworkWired, faEnvelopeOpen, faDesktop, faIdBadge, faCog, faSpinner,
      faArrowUpRightFromSquare, faCaretDown, faAddressCard, faBroadcastTower, faBuildingShield,
      faCertificate, faCubes, faDashboard, faDesktop, faDiagramProject, faGear, faGlobe,
      faMicrochip, faNetworkWired, faShieldHalved, faStamp, faTag, faUser, faUsers,
      faUsersBetweenLines, faXmarksLines, faFileCirclePlus, faBoxArchive, faFileCircleCheck,
      faPaste, faEraser, faBug, faRulerCombined, faCirclePlay, faCircleXmark, faShareNodes,
      faMaximize, faIcons, faMinusCircle);
    faConfig.fixedWidth = true;
  }
}
