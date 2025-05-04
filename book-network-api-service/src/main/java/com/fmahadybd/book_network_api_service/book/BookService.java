package com.fmahadybd.book_network_api_service.book;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fmahadybd.book_network_api_service.common.PageResponse;
import com.fmahadybd.book_network_api_service.exception.OperationNotPermittedException;
import com.fmahadybd.book_network_api_service.file.FileStorageService;
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
  private final FileStorageService fileStorageService;

  public Integer save(BookRequest request, Authentication connectedUser) {
    User user = ((User) connectedUser.getPrincipal());
    Book book = bookMapper.toBook(request);
    book.setOwner(user);

    System.out.println(book);

    return bookRepository.save(book).getId();
  }

  public PageResponse<BookResponse> findAllBooks(int page, int size) {
    // todo only display shareable and non archived books
    Pageable pageable = PageRequest.of(page, size);
    Page<Book> books = bookRepository.findAll(pageable);
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

  public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
    User user = ((User) connectedUser.getPrincipal());
    Pageable pageable = PageRequest.of(page, size);
    Page<Book> books = bookRepository.findAll(withOwnerId(user.getId()), pageable);
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

  @Transactional
  public Integer updateShareableStatus(Integer bookId, boolean shareable, Authentication connectedUser) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    User user = ((User) connectedUser.getPrincipal());
    if (!Objects.equals(book.getOwner().getId(), user.getId())) {
      throw new OperationNotPermittedException("You cannot update others books shareable status");
    }
    book.setShareable(shareable);
    bookRepository.save(book);
    return bookId;
  }

  @Transactional
  public Integer updateArchivedStatus(Integer bookId, boolean archived, Authentication connectedUser) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    User user = ((User) connectedUser.getPrincipal());
    if (!Objects.equals(book.getOwner().getId(), user.getId())) {
      throw new OperationNotPermittedException("You cannot update others books archived status");
    }
    book.setArchived(archived);
    bookRepository.save(book);
    return bookId;
  }

  @Transactional
  public Integer borrowBook(Integer bookId, Authentication connectedUser) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    if (book.isArchived() || !book.isShareable()) {
      throw new OperationNotPermittedException(
          "The requested book cannot be borrowed since it is archived or not shareable");
    }
    User user = ((User) connectedUser.getPrincipal());
    if (Objects.equals(book.getOwner().getId(), user.getId())) {
      throw new OperationNotPermittedException("You cannot borrow your own book");
    }
    final boolean isAlreadyBorrowedByUser = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
    if (isAlreadyBorrowedByUser) {
      throw new OperationNotPermittedException(
          "You already borrowed this book and it is still not returned or the return is not approved by the owner");
    }

    final boolean isAlreadyBorrowedByOtherUser = transactionHistoryRepository.isAlreadyBorrowed(bookId);
    if (isAlreadyBorrowedByOtherUser) {
      throw new OperationNotPermittedException("Te requested book is already borrowed");
    }

    BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
        .user(user)
        .book(book)
        .returned(false)
        .returnApproved(false)
        .build();
    return transactionHistoryRepository.save(bookTransactionHistory).getId();

  }

  @Transactional
  public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    if (book.isArchived() || !book.isShareable()) {
      throw new OperationNotPermittedException("The requested book is archived or not shareable");
    }
    User user = ((User) connectedUser.getPrincipal());
    if (Objects.equals(book.getOwner().getId(), user.getId())) {
      throw new OperationNotPermittedException("You cannot borrow or return your own book");
    }

    BookTransactionHistory bookTransactionHistory = transactionHistoryRepository
        .findByBookIdAndUserId(bookId, user.getId())
        .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));

    bookTransactionHistory.setReturned(true);
    return transactionHistoryRepository.save(bookTransactionHistory).getId();
  }

  @Transactional
  public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    if (book.isArchived() || !book.isShareable()) {
      throw new OperationNotPermittedException("The requested book is archived or not shareable");
    }
    User user = ((User) connectedUser.getPrincipal());
    if (!Objects.equals(book.getOwner().getId(), user.getId())) {
      throw new OperationNotPermittedException("You cannot approve the return of a book you do not own");
    }

    BookTransactionHistory bookTransactionHistory = transactionHistoryRepository
        .findByBookIdAndUserId(bookId, user.getId())
        .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));

    bookTransactionHistory.setReturnApproved(true);
    return transactionHistoryRepository.save(bookTransactionHistory).getId();
  }

  public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
    User user = ((User) connectedUser.getPrincipal());
    var bookCover = fileStorageService.saveFile(file, user.getId());
    book.setBookCover(bookCover);
    bookRepository.save(book);
  }

}
