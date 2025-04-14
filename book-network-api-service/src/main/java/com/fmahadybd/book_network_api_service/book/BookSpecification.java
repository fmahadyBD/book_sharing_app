package com.fmahadybd.book_network_api_service.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
  public static Specification<Book> withOwnerId(Integer ownerId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"), ownerId);
  }
}
