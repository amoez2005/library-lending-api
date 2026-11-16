package io.github.amoez2005.library.book;

import jakarta.persistence.*;

/** Persisted library title with optimistic concurrency control. */
@Entity
@Table(
    name = "books",
    uniqueConstraints = @UniqueConstraint(name = "uk_book_isbn", columnNames = "isbn"))
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, length = 120)
  private String author;

  @Column(nullable = false, length = 13)
  private String isbn;

  @Column(nullable = false)
  private boolean borrowed;

  /** JPA constructor. */
  protected Book() {}

  /** Creates an available book. */
  public Book(String title, String author, String isbn) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
  }

  /** Replaces bibliographic details without changing loan state. */
  public void update(BookRequest request) {
    title = request.title();
    author = request.author();
    isbn = request.isbn();
  }

  /** Marks an available title as borrowed. */
  public void borrow() {
    if (borrowed) throw new BookConflictException("Book is already borrowed.");
    borrowed = true;
  }

  /** Returns a title that is currently borrowed. */
  public void returnToShelf() {
    if (!borrowed) throw new BookConflictException("Book is already available.");
    borrowed = false;
  }

  /** Returns the generated resource ID. */
  public Long getId() {
    return id;
  }

  /** Returns the optimistic lock version. */
  public Long getVersion() {
    return version;
  }

  /** Returns the title. */
  public String getTitle() {
    return title;
  }

  /** Returns the author. */
  public String getAuthor() {
    return author;
  }

  /** Returns the ISBN. */
  public String getIsbn() {
    return isbn;
  }

  /** Returns whether the book has an active loan. */
  public boolean isBorrowed() {
    return borrowed;
  }
}
