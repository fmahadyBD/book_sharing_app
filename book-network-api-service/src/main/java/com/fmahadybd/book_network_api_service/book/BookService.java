package com.fmahadybd.book_network_api_service.book;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fmahadybd.book_network_api_service.common.PageResponse;
import com.fmahadybd.book_network_api_service.hostory.BookTransactionHistory;
import com.fmahadybd.book_network_api_service.hostory.BookTransactionHistoryRepository;
import com.fmahadybd.book_network_api_service.user.User;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import static com.fmahadybd.book_network_api_service.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {
  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final BookTransactionHistoryRepository transactionHistoryRepository;

  public Integer save(BookRequest request, Authentication connectedUser) {
    User user = ((User) connectedUser.getPrincipal());
    Book book = bookMapper.toBook(request);
    book.setOwner(user);
    return bookRepository.save(book).getId();
  }

  public BookResponse findById(Integer bookId) {
    return bookRepository.findById(bookId)
        .map(bookMapper::toBookResponse)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
  }

  public PageResponse<BookResponse> findAll(int page, int size, Authentication connectedUser) {

    // User user = ((User) connectedUser.getPrincipal());

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
    Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, connectedUser.getName());

    List<BookResponse> bookResponses = books.stream()
        .map(bookMapper::toBookResponse)
        .toList();
    return new PageResponse<>(
        bookResponses,
        books.getNumber(),
        books.getSize(),
        books.getTotalElements(),
        books.getTotalPages(),
        books.isFirst(),
        books.isLast());
  }

  public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
    // User user = ((User) connectedUser.getPrincipal());
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
    Page<Book> books = bookRepository.findAll(withOwnerId(connectedUser.getName()), pageable);
    List<BookResponse> booksResponse = books.stream()
        .map(bookMapper::toBookResponse)
        .toList();
    return new PageResponse<>(
        booksResponse,
        books.getNumber(),
        books.getSize(),
        books.getTotalElements(),
        books.getTotalPages(),
        books.isFirst(),
        books.isLast());
  }

  public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        // User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllBorrowedBooks(pageable, connectedUser.getName());
        List<BorrowedBookResponse> booksResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                booksResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

}
