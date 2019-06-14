/* tslint:disable:max-line-length */
import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';
import {CookieService} from 'ngx-cookie-service';
import {routing} from './app.routes';
import {LogoutComponent} from './auth/logout.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {LoginComponent} from './auth/login.component';
import {ForgotPasswordComponent} from './auth/forgot-password.component';
import {JwtModule} from '@auth0/angular-jwt';
import {FlexLayoutModule} from '@angular/flex-layout';
import {NgProgressModule} from '@ngx-progressbar/core';
import {AppConstants} from './app.constants';
import {RxStompService} from '@stomp/ng2-stompjs';
import {
  MatButtonModule,
  MatCardModule,
  MatDialogModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatSnackBarModule,
  MatToolbarModule
} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';
import {NgProgressHttpModule} from '@ngx-progressbar/http';
import {QFormsModule} from '@eurodyn/forms';
import {HeaderComponent} from './shared/layout/header.component';
import {FooterComponent} from './shared/layout/footer.component';
import {SidenavComponent} from './shared/layout/sidenav.component';
import {ContainersModule} from './shared/component/containers/containers.module';
import {DisplayModule} from './shared/component/display/display.module';
import {CanActivateGuard} from './shared/guards/can-activate-guard';
import {ContainerDeployComponent} from './shared/component/containers/container-deploy.component';
import {OkCancelModalComponent} from './shared/component/display/ok-cancel-modal/ok-cancel-modal.component';

export function getJwtToken(): string {
  return localStorage.getItem(AppConstants.JWT_STORAGE_NAME);
}

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    SidenavComponent,
    LoginComponent,
    LogoutComponent,
    NewPasswordComponent,
    ForgotPasswordComponent
  ],
  imports: [
    BrowserModule,
    FlexLayoutModule,
    BrowserAnimationsModule,
    routing,
    HttpClientModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: getJwtToken,
        whitelistedDomains: new Array(new RegExp('^null$'))
      }
    }),
    MatMenuModule,
    MatDialogModule,
    MatToolbarModule,
    MatListModule,
    MatCardModule,
    MatIconModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatInputModule,
    MatButtonModule,
    ContainersModule,
    DisplayModule,
    NgProgressModule.withConfig({
      trickleSpeed: 500,
      debounceTime: 500,
      meteor: false,
      spinner: false,
      thick: false,
      color: '#50A7D7'
    }),
    NgProgressHttpModule
  ],
  exports: [],
  providers: [
    CookieService,
    CanActivateGuard,
    RxStompService,
    QFormsModule,
  ],
  bootstrap: [AppComponent],
  entryComponents: [ContainerDeployComponent, OkCancelModalComponent],
})
export class AppModule {
}
