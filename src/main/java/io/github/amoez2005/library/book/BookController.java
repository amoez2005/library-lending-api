package io.github.amoez2005.library.book;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Stateless HTTP boundary for the book catalog and loan transitions. */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Catalog CRUD and discoverable loan transitions")
public class BookController {
  private final BookService service;
  private final BookModelAssembler assembler;

  /** Injects business rules and HAL representation mapping. */
  public BookController(BookService service, BookModelAssembler assembler) {
    this.service = service;
    this.assembler = assembler;
  }

  /** Lists matching titles with page navigation and mandatory cache revalidation. */
  @GetMapping
  @Operation(summary = "Search the catalog with zero-based pagination")
  public ResponseEntity<EntityModel<Map<String, Object>>> list(
      @RequestParam(defaultValue = "") @Size(max = 160) String q,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "12") @Min(1) @Max(100) int size) {
    Page<Book> result = service.list(q, page, size);
    var model =
        EntityModel.of(
            Map.<String, Object>of(
                "items",
                result.getContent().stream().map(assembler::toModel).toList(),
                "page",
                page,
                "size",
                size,
                "totalElements",
                result.getTotalElements(),
                "totalPages",
                result.getTotalPages()));
    model.add(linkTo(methodOn(BookController.class).list(q, page, size)).withSelfRel());
    model.add(linkTo(BookController.class).withRel("create").withType("application/json"));
    if (result.hasNext())
      model.add(linkTo(methodOn(BookController.class).list(q, page + 1, size)).withRel("next"));
    if (result.hasPrevious())
      model.add(linkTo(methodOn(BookController.class).list(q, page - 1, size)).withRel("prev"));
    return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(model);
  }

  /** Returns a single title; a filter implements conditional GET via an ETag. */
  @GetMapping("/{id}")
  @Operation(summary = "Get a book (supports If-None-Match and 304)")
  public ResponseEntity<EntityModel<Book>> get(@PathVariable long id) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(assembler.toModel(service.get(id)));
  }

  /** Creates a title with a canonical Location header. */
  @PostMapping
  @Operation(summary = "Create an available book")
  public ResponseEntity<EntityModel<Book>> create(@Valid @RequestBody BookRequest request) {
    Book book = service.create(request);
    return ResponseEntity.created(linkTo(methodOn(BookController.class).get(book.getId())).toUri())
        .cacheControl(CacheControl.noStore())
        .body(assembler.toModel(book));
  }

  /** Replaces bibliographic fields of an existing resource. */
  @PutMapping("/{id}")
  @Operation(summary = "Replace book details; preserve lending state")
  public ResponseEntity<EntityModel<Book>> update(
      @PathVariable long id, @Valid @RequestBody BookRequest request) {
    return changed(service.update(id, request));
  }

  /** Deletes an available resource. */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an available book")
  public ResponseEntity<Void> delete(@PathVariable long id) {
    service.delete(id);
    return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
  }

  /** Borrows an available book; repeated borrowing is a conflict. */
  @PostMapping("/{id}/borrow")
  @Operation(summary = "Borrow an available book")
  public ResponseEntity<EntityModel<Book>> borrow(@PathVariable long id) {
    return changed(service.borrow(id));
  }

  /** Returns a borrowed book; repeated return is a conflict. */
  @PostMapping("/{id}/return")
  @Operation(summary = "Return a borrowed book")
  public ResponseEntity<EntityModel<Book>> returnBook(@PathVariable long id) {
    return changed(service.returnBook(id));
  }

  private ResponseEntity<EntityModel<Book>> changed(Book book) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(assembler.toModel(book));
  }
}
