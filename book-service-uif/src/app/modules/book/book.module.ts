import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BookRoutingModule } from './book-routing.module';
import { MyBooksComponent } from './my-books/my-books.component';
import { BookCardComponent } from './component/book-card/book-card.component';
import { MenuComponent } from './component/menu/menu.component';
import { RattingComponent } from './component/ratting/ratting.component';
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { ManageBookComponent } from './pages/manage-book/manage-book.component';
import { FormsModule } from '@angular/forms';
import { BorrowedBookListComponent } from './pages/borrowed-book-list/borrowed-book-list.component';
import { ReturnedBookComponent } from './pages/returned-book/returned-book.component';


@NgModule({
  declarations: [
    MyBooksComponent,
    MenuComponent,
    MainComponent,
    BookCardComponent,
    RattingComponent,
    BookListComponent,
    ManageBookComponent,
    BorrowedBookListComponent,
    ReturnedBookComponent,
    
    
  ],
  imports: [
    CommonModule,
    BookRoutingModule,
    FormsModule
  ]
})
export class BookModule { }
