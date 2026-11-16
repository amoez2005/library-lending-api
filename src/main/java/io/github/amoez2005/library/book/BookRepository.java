package io.github.amoez2005.library.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access abstraction implemented by Spring Data JPA. */
public interface BookRepository extends JpaRepository<Book, Long> {
  /** Finds titles containing a case-insensitive search phrase. */
  Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  /** Checks the unique ISBN before inserting or updating. */
  boolean existsByIsbnAndIdNot(String isbn, Long id);
}
