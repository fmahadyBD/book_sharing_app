package com.fmahadybd.book_network_api_service.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Integer> {


    @Query("""
            SELECT book
            FROM BOOk book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.createdBy != :userId
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, String name);

}
