import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {Route1Component} from './route1/route1.component';
import {Route2Component} from './route2/route2.component';
import {AuthConfigModule} from './auth/auth-config.module';
import {AuthInterceptor} from "angular-auth-oidc-client";

@NgModule({
  declarations: [
    AppComponent,
    Route1Component,
    Route2Component,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    AuthConfigModule,
    HttpClientModule,
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
