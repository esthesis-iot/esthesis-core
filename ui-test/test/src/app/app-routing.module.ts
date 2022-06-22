import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Route1Component} from './route1/route1.component';
import {Route2Component} from './route2/route2.component';
import {AutoLoginAllRoutesGuard} from "angular-auth-oidc-client";

const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: 'route1'},
  {path: "route1", component: Route1Component, canActivate: [AutoLoginAllRoutesGuard]},
  {path: "route2", component: Route2Component, canActivate: [AutoLoginAllRoutesGuard]},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
