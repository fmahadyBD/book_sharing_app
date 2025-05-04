import { Component, OnInit } from '@angular/core';
import { BookService } from '../../../services/services';
import { Router } from '@angular/router';
import { PageResponseBookResponse } from '../../../services/models';

@Component({
  selector: 'app-book-list',
  standalone: false,
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.scss'
})
export class BookListComponent implements OnInit {

  bookResponse: PageResponseBookResponse = {};
  page = 0;
  size = 5;

  constructor(
    private bookService: BookService,
    private router: Router
  ) { }
  ngOnInit(): void {

    this.findAllBooks();
  }
  private findAllBooks() {
    this.bookService.findAllBooks({
      page: this.page,
      size: this.size
    }
    ).subscribe({
      next: (books) => {
        this.bookResponse = books;
      }
    })
  }
}
