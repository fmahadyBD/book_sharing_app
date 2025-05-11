import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BookRoutingModule } from './book-routing.module';
import { MyBooksComponent } from './my-books/my-books.component';
import { BookCardComponent } from './component/book-card/book-card.component';
import { MenuComponent } from './component/menu/menu.component';
import { RattingComponent } from './component/ratting/ratting.component';
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';


@NgModule({
  declarations: [
    MyBooksComponent,
    MenuComponent,
    MainComponent,
    BookCardComponent,
    RattingComponent,
    BookListComponent
    
  ],
  imports: [
    CommonModule,
    BookRoutingModule,
  ]
})
export class BookModule { }
