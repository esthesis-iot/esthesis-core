import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TagsComponent} from './tags.component';
import {TagEditComponent} from './tag-edit.component';

const routes: Routes = [
  {path: '', component: TagsComponent},
  {path: ':id', component: TagEditComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TagsRoutingModule {
}
