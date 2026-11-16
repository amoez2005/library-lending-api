package io.github.amoez2005.library.book;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
  @Mock BookRepository repository;
  BookService service;
  final BookRequest request = new BookRequest("Clean Code", "Robert Martin", "9780132350884");

  @BeforeEach
  void setup() {
    service = new BookService(repository);
  }

  @Test
  void createsUniqueBook() {
    when(repository.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    assertThat(service.create(request).getTitle()).isEqualTo("Clean Code");
  }

  @Test
  void rejectsDuplicateIsbn() {
    when(repository.existsByIsbnAndIdNot(request.isbn(), -1L)).thenReturn(true);
    assertThatThrownBy(() -> service.create(request)).isInstanceOf(BookConflictException.class);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void rejectsMissingBook() {
    assertThatThrownBy(() -> service.get(42))
        .isInstanceOf(BookNotFoundException.class)
        .hasMessageContaining("42");
  }

  @Test
  void normalizesSearchAndUsesBoundedPage() {
    when(repository.findByTitleContainingIgnoreCase(eq("Clean"), any())).thenReturn(Page.empty());
    assertThat(service.list(" Clean ", 0, 10)).isEmpty();
    verify(repository)
        .findByTitleContainingIgnoreCase(eq("Clean"), argThat(page -> page.getPageSize() == 10));
  }

  @Test
  void updatesExistingBook() {
    Book book = existing();
    when(repository.saveAndFlush(book)).thenReturn(book);
    assertThat(service.update(1, request).getTitle()).isEqualTo("Clean Code");
  }

  @Test
  void borrowsAndReturnsExistingBook() {
    Book book = existing();
    when(repository.saveAndFlush(book)).thenReturn(book);
    assertThat(service.borrow(1).isBorrowed()).isTrue();
    assertThat(service.returnBook(1).isBorrowed()).isFalse();
  }

  @Test
  void deletesAvailableBook() {
    Book book = existing();
    service.delete(1);
    verify(repository).delete(book);
    verify(repository).flush();
  }

  @Test
  void refusesToDeleteBorrowedBook() {
    existing().borrow();
    assertThatThrownBy(() -> service.delete(1)).isInstanceOf(BookConflictException.class);
    verify(repository, never()).delete(any());
  }

  private Book existing() {
    Book book = new Book("Old", "Writer", "9780201616224");
    when(repository.findById(1L)).thenReturn(Optional.of(book));
    return book;
  }
}
