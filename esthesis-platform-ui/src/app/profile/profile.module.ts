import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ProfileRoutingModule} from './profile-routing.module';
import {ProfileComponent} from './profile.component';
import {ProfileDetailsComponent} from './profile-details.component';
import {MatCardModule, MatFormFieldModule, MatInputModule, MatTabsModule} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';

@NgModule({
  declarations: [
    ProfileComponent,
    ProfileDetailsComponent,
  ],
  imports: [
    CommonModule,
    ProfileRoutingModule,
    MatCardModule,
    MatTabsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
  ]
})
export class ProfileModule {
}
