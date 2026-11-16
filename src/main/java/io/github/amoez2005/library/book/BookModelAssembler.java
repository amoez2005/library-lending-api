package io.github.amoez2005.library.book;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

/** Builds HAL representations with state-dependent legal transition links. */
@Component
public class BookModelAssembler {
  /** Exposes data and discoverable HTTP actions for one title. */
  public EntityModel<Book> toModel(Book book) {
    String url = linkTo(methodOn(BookController.class).get(book.getId())).toUri().toString();
    return EntityModel.of(
        book,
        Link.of(url).withSelfRel(),
        linkTo(methodOn(BookController.class).list("", 0, 12)).withRel("collection"),
        Link.of(url).withRel("update").withType("PUT"),
        Link.of(url + (book.isBorrowed() ? "/return" : "/borrow"))
            .withRel(book.isBorrowed() ? "return" : "borrow")
            .withType("POST"));
  }
}
