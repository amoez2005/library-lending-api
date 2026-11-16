package io.github.amoez2005.library.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies lending rules independently from HTTP and persistence implementation. */
@Service
@Transactional
public class BookService {
  private final BookRepository repository;

  /** Injects the persistence port. */
  public BookService(BookRepository repository) {
    this.repository = repository;
  }

  /** Lists a bounded page of matching titles in a stable ID order. */
  @Transactional(readOnly = true)
  public Page<Book> list(String query, int page, int size) {
    return repository.findByTitleContainingIgnoreCase(
        query.strip(), PageRequest.of(page, size, Sort.by("id")));
  }

  /** Looks up a resource or raises a domain-specific not-found error. */
  @Transactional(readOnly = true)
  public Book get(long id) {
    return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
  }

  /** Adds a unique ISBN and flushes to expose constraint failures immediately. */
  public Book create(BookRequest request) {
    checkIsbn(request.isbn(), -1L);
    return repository.saveAndFlush(new Book(request.title(), request.author(), request.isbn()));
  }

  /** Updates a title while retaining its loan status. */
  public Book update(long id, BookRequest request) {
    Book book = get(id);
    checkIsbn(request.isbn(), id);
    book.update(request);
    return repository.saveAndFlush(book);
  }

  /** Removes an available title; active loans must be returned first. */
  public void delete(long id) {
    Book book = get(id);
    if (book.isBorrowed()) throw new BookConflictException("Return the book before deleting it.");
    repository.delete(book);
    repository.flush();
  }

  /** Creates a loan using the domain state transition. */
  public Book borrow(long id) {
    Book book = get(id);
    book.borrow();
    return repository.saveAndFlush(book);
  }

  /** Completes a loan using the domain state transition. */
  public Book returnBook(long id) {
    Book book = get(id);
    book.returnToShelf();
    return repository.saveAndFlush(book);
  }

  private void checkIsbn(String isbn, Long id) {
    if (repository.existsByIsbnAndIdNot(isbn, id))
      throw new BookConflictException("ISBN already exists.");
  }
}
