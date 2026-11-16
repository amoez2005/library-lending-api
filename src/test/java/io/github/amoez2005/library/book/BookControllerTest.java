package io.github.amoez2005.library.book;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class BookControllerTest {
  @Test
  void delegatesMutationAndReturnsCorrectHttpSemantics() {
    BookService service = mock(BookService.class);
    BookModelAssembler assembler = new BookModelAssembler();
    BookController controller = new BookController(service, assembler);
    Book book = mock(Book.class);
    when(book.getId()).thenReturn(1L);
    BookRequest request = new BookRequest("Title", "Author", "9780132350884");
    when(service.create(request)).thenReturn(book);
    when(service.get(1)).thenReturn(book);
    when(service.update(1, request)).thenReturn(book);
    when(service.borrow(1)).thenReturn(book);
    when(service.returnBook(1)).thenReturn(book);
    assertThat(controller.create(request).getStatusCode().value()).isEqualTo(201);
    assertThat(controller.get(1).getHeaders().getCacheControl()).isEqualTo("no-cache");
    assertThat(controller.update(1, request).getBody().getContent()).isSameAs(book);
    assertThat(controller.borrow(1).getStatusCode().value()).isEqualTo(200);
    assertThat(controller.returnBook(1).getStatusCode().value()).isEqualTo(200);
    assertThat(controller.delete(1).getStatusCode().value()).isEqualTo(204);
    verify(service).delete(1);
  }

  @Test
  void emptyCatalogStillOffersCreationLink() {
    BookService service = mock(BookService.class);
    when(service.list("", 0, 12)).thenReturn(new PageImpl<>(List.of()));
    var model = new BookController(service, new BookModelAssembler()).list("", 0, 12).getBody();
    assertThat(model.getRequiredLink("create").getType()).isEqualTo("application/json");
    assertThat(model.getContent().get("totalElements")).isEqualTo(0L);
  }
}
