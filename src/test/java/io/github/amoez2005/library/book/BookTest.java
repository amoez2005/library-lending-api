package io.github.amoez2005.library.book;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookTest {
  @Test
  void transitionsGuardAgainstDuplicateLoansAndReturns() {
    Book book = new Book("Clean Code", "Robert Martin", "9780132350884");
    assertThat(book.isBorrowed()).isFalse();
    assertThatThrownBy(book::returnToShelf).isInstanceOf(BookConflictException.class);
    book.borrow();
    assertThat(book.isBorrowed()).isTrue();
    assertThatThrownBy(book::borrow).isInstanceOf(BookConflictException.class);
    book.returnToShelf();
    assertThat(book.isBorrowed()).isFalse();
  }

  @Test
  void updatesDetailsWithoutChangingLoanState() {
    Book book = new Book("Old", "Author", "9780132350884");
    book.borrow();
    book.update(new BookRequest(" New ", " Writer ", "9780201616224"));
    assertThat(book.getTitle()).isEqualTo("New");
    assertThat(book.getAuthor()).isEqualTo("Writer");
    assertThat(book.getIsbn()).isEqualTo("9780201616224");
    assertThat(book.isBorrowed()).isTrue();
    assertThat(book.getId()).isNull();
    assertThat(book.getVersion()).isNull();
  }

  @Test
  void nullInputsRemainAvailableToBeanValidation() {
    BookRequest request = new BookRequest(null, null, null);
    assertThat(request.title()).isNull();
    assertThat(request.author()).isNull();
    assertThat(request.isbn()).isNull();
  }
}
