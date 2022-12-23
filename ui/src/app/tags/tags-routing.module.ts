import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TagsListComponent} from './tags-list/tags-list.component';
import {TagEditComponent} from './tag-edit/tag-edit.component';

const routes: Routes = [
  {path: '', component: TagsListComponent},
  {path: ':id', component: TagEditComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TagsRoutingModule {
}
