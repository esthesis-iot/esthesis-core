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
import {CookieService} from 'ngx-cookie-service';
import {AppComponent} from './app.component';
import {AppConstants} from './app.constants';
import {routing} from './app.routes';
import {ForgotPasswordComponent} from './auth/forgot-password.component';
import {LoginComponent} from './auth/login.component';
import {LogoutComponent} from './auth/logout.component';
import {NewPasswordComponent} from './auth/new-password.component';
import {DisplayModule} from './shared/component/display/display.module';
import {OkCancelModalComponent} from './shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {TextModalComponent} from './shared/component/display/text-modal/text-modal.component';
import {FooterComponent} from './shared/layout/footer.component';
import {HeaderComponent} from './shared/layout/header.component';
import {SidenavComponent} from './shared/layout/sidenav.component';
import {QFormsModule} from '@qlack/forms';
import {QFormValidationModule} from '@qlack/form-validation';
import {NgProgressModule} from "ngx-progressbar";
import {NgProgressHttpModule} from "ngx-progressbar/http";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatCheckboxModule } from '@angular/material/checkbox';

export function getJwtToken(): string | null {
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
    imports: [
        BrowserModule,
        FlexLayoutModule,
        BrowserAnimationsModule,
        routing,
        HttpClientModule,
        JwtModule.forRoot({
            config: {
                tokenGetter: getJwtToken,
                allowedDomains: new Array(new RegExp('^null$'))
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
        MatAutocompleteModule
    ],
    providers: [
        CookieService,
        QFormsModule,
        QFormValidationModule
    ]
})

export class AppModule {
}
