import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from '../pages/main/main.component';
import { BookListComponent } from '../pages/book-list/book-list.component';

const routes: Routes = [
  {path:'',
    component: MainComponent,
    children:[
      {
        path: '',
        component: BookListComponent
      }
    ]
  }
  
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BookRoutingModule { }
