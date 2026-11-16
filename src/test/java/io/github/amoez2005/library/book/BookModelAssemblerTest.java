package io.github.amoez2005.library.book;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class BookModelAssemblerTest {
  @Test
  void exposesOnlyTheValidLoanTransition() {
    Book book = mock(Book.class);
    when(book.getId()).thenReturn(7L);
    var assembler = new BookModelAssembler();
    var available = assembler.toModel(book);
    assertThat(available.getRequiredLink("borrow").getHref()).endsWith("/7/borrow");
    assertThat(available.hasLink("return")).isFalse();
    when(book.isBorrowed()).thenReturn(true);
    var borrowed = assembler.toModel(book);
    assertThat(borrowed.getRequiredLink("return").getHref()).endsWith("/7/return");
    assertThat(borrowed.hasLink("borrow")).isFalse();
  }
}
