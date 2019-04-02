/* tslint:disable:max-line-length */
import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';
import {CookieService} from 'ngx-cookie-service';
import {routing} from './app.routes';
import {HeaderComponent} from './layout/header.component';
import {FooterComponent} from './layout/footer.component';
import {SidenavComponent} from './layout/sidenav.component';
import {LogoutComponent} from './auth/logout.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {LoginComponent} from './auth/login.component';
import {ForgotPasswordComponent} from './auth/forgot-password.component';
import {JwtModule} from '@auth0/angular-jwt';
import {FlexLayoutModule} from '@angular/flex-layout';
import {NgProgressModule} from '@ngx-progressbar/core';
import {AppConstants} from './app.constants';
import {CanActivateGuard} from './guards/can-activate-guard';
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
import {ContainerDeployComponent} from './shared/containers/container-deploy.component';
import {OkCancelModalComponent} from './shared/display/ok-cancel-modal/ok-cancel-modal.component';
import {ContainersModule} from './shared/containers/containers.module';
import {DisplayModule} from './shared/display/display.module';
import {NgProgressHttpModule} from '@ngx-progressbar/http';

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
    RxStompService
  ],
  bootstrap: [AppComponent],
  entryComponents: [ContainerDeployComponent, OkCancelModalComponent],
})
export class AppModule {
}
