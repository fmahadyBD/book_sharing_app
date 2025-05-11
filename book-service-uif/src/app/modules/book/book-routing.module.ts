import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { MyBooksComponent } from './my-books/my-books.component';
import { ManageBookComponent } from './pages/manage-book/manage-book.component';
import { BorrowedBookListComponent } from './pages/borrowed-book-list/borrowed-book-list.component';
import { ReturnedBookComponent } from './pages/returned-book/returned-book.component';
import { authGuardGuard } from '../../services/gurd/auth-guard.guard';

const routes: Routes = [
  {path:'',
    component: MainComponent,
    children:[
      {
        path: '',
        component: BookListComponent,
        canActivate:[authGuardGuard]
      },
      {
        path:'my-books',
        component:MyBooksComponent,
        canActivate:[authGuardGuard]

      },{
        path:'manage',
        component:ManageBookComponent,
        canActivate:[authGuardGuard]
      },{
        path: 'manage/:bookId',
        component: ManageBookComponent,
        canActivate:[authGuardGuard]
      },
      {
        path: 'my-borrowed-books',
        component: BorrowedBookListComponent,
        canActivate:[authGuardGuard]
      
      },{
        path:'my-returned-books',
        component: ReturnedBookComponent,
        canActivate:[authGuardGuard]
      }

    ]
  }
  
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BookRoutingModule { }
