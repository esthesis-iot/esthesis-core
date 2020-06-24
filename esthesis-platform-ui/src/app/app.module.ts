/* tslint:disable:max-line-length */
import {HttpClientModule} from '@angular/common/http';
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
import {JwtModule} from '@auth0/angular-jwt';
import {QFormsModule} from '@eurodyn/forms';
import {RxStompService} from '@stomp/ng2-stompjs';
import {CookieService} from 'ngx-cookie-service';
import {NgProgressModule} from 'ngx-progressbar';
import { NgProgressHttpModule } from 'ngx-progressbar/http';
import {AppComponent} from './app.component';
import {AppConstants} from './app.constants';
import {routing} from './app.routes';
import {ForgotPasswordComponent} from './auth/forgot-password.component';
import {LoginComponent} from './auth/login.component';
import {LogoutComponent} from './auth/logout.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {CommandComponent} from './shared/component/commands/command.component';
import {CommandsModule} from './shared/component/commands/commands.module';
import {DisplayModule} from './shared/component/display/display.module';
import {OkCancelModalComponent} from './shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {TextModalComponent} from './shared/component/display/text-modal/text-modal.component';
import {FooterComponent} from './shared/layout/footer.component';
import {HeaderComponent} from './shared/layout/header.component';
import {SidenavComponent} from './shared/layout/sidenav.component';

export function getJwtToken(): string {
  return localStorage.getItem(AppConstants.JWT_STORAGE_NAME);
}

@NgModule({
  bootstrap: [AppComponent],
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
  entryComponents: [OkCancelModalComponent, CommandComponent, TextModalComponent],
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
    CommandsModule,
    DisplayModule,
    NgProgressModule.withConfig({
      color: '#50A7D7',
      debounceTime: 500,
      meteor: false,
      spinner: false,
      thick: false,
      trickleSpeed: 500
    }),
    NgProgressHttpModule
  ],
  providers: [
    CookieService,
    RxStompService,
    QFormsModule,
  ],
})
export class AppModule {
}
