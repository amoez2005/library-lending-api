package io.github.amoez2005.library.book;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class BookRepositoryTest {
  @Autowired BookRepository repository;

  @Test
  void persistsVersionsAndQueriesIsbnAndCaseInsensitiveTitles() {
    Book saved = repository.saveAndFlush(new Book("Clean Code", "Martin", "9780132350884"));
    assertThat(saved.getId()).isPositive();
    assertThat(saved.getVersion()).isZero();
    assertThat(repository.findByTitleContainingIgnoreCase("CLEAN", PageRequest.of(0, 10)))
        .hasSize(1);
    assertThat(repository.existsByIsbnAndIdNot(saved.getIsbn(), -1L)).isTrue();
    assertThat(repository.existsByIsbnAndIdNot(saved.getIsbn(), saved.getId())).isFalse();
    saved.borrow();
    repository.saveAndFlush(saved);
    assertThat(saved.getVersion()).isEqualTo(1L);
  }
}
