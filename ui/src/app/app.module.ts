/* tslint:disable:max-line-length */
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ReactiveFormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatMenuModule} from '@angular/material/menu';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatToolbarModule} from '@angular/material/toolbar';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {CookieService} from 'ngx-cookie-service';
import {AppComponent} from './app.component';
import {routing} from './app.routes';
import {ForgotPasswordComponent} from './auth/forgot-password.component';
import {LogoutComponent} from './auth/logout.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {DisplayModule} from './shared/component/display/display.module';
import {FooterComponent} from './shared/layout/footer.component';
import {HeaderComponent} from './shared/layout/header.component';
import {SidenavComponent} from './shared/layout/sidenav.component';
import {QFormsModule} from '@qlack/forms';
import {QFormValidationModule} from '@qlack/form-validation';
import {NgProgressModule} from "ngx-progressbar";
import {NgProgressHttpModule} from "ngx-progressbar/http";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatCheckboxModule} from '@angular/material/checkbox';
import {NgxDarkModule} from 'ngx-dark';
import {AuthInterceptor} from "angular-auth-oidc-client";
import {AuthConfigModule} from "./auth/auth-config.module";

@NgModule({
  bootstrap: [AppComponent],
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    SidenavComponent,
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
    MatMenuModule,
    MatDialogModule,
    MatToolbarModule,
    MatListModule,
    MatCardModule,
    MatIconModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatCheckboxModule,
    MatInputModule,
    MatButtonModule,
    DisplayModule,
    HttpClientModule,
    NgProgressModule.withConfig({
      color: '#50A7D7',
      debounceTime: 500,
      meteor: false,
      spinner: false,
      thick: true,
      trickleSpeed: 500
    }),
    NgProgressHttpModule,
    MatAutocompleteModule,
    NgxDarkModule,
    AuthConfigModule
  ],
  providers: [
    CookieService,
    QFormsModule,
    QFormValidationModule,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ]
})

export class AppModule {
}
