package io.github.amoez2005.library.book;

/** Indicates an operation incompatible with the current resource state. */
public class BookConflictException extends RuntimeException {
  /** Creates a conflict with a safe public explanation. */
  public BookConflictException(String message) {
    super(message);
  }
}
