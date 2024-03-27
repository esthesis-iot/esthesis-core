import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {UserRoutingModule} from "./user-routing.module";
import {UserProfileComponent} from "./user-profile/user-profile.component";
import {ComponentsModule} from "../shared/components/components.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {ReactiveFormsModule} from "@angular/forms";
import { UserSignoutComponent } from './user-signout/user-signout.component';
import { UserThemesComponent } from './user-themes/user-themes.component';


@NgModule({
  declarations: [
    UserProfileComponent,
    UserSignoutComponent,
    UserThemesComponent
  ],
  imports: [
    CommonModule,
    UserRoutingModule,
    ComponentsModule,
    FontAwesomeModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule
  ]
})
export class UserModule { }
