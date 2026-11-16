package io.github.amoez2005.library.book;

/** Indicates that an identified resource does not exist. */
public class BookNotFoundException extends RuntimeException {
  /** Creates an error for the requested ID. */
  public BookNotFoundException(long id) {
    super("Book " + id + " was not found.");
  }
}
