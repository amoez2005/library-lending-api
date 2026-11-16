package io.github.amoez2005.library.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Validated bibliographic input; ISBNs use exactly 13 digits. */
public record BookRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 120) String author,
    @NotBlank @Pattern(regexp = "[0-9]{13}", message = "must contain exactly 13 digits")
        String isbn) {
  /** Normalizes human-readable fields before validation and storage. */
  public BookRequest {
    title = title == null ? null : title.strip();
    author = author == null ? null : author.strip();
    isbn = isbn == null ? null : isbn.strip();
  }
}
