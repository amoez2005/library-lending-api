package io.github.amoez2005.library.api;

import static org.assertj.core.api.Assertions.*;

import io.github.amoez2005.library.book.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;

class ApiComponentsTest {
  @Test
  void configuresMetadataAndApiOnlyEtagFilter() {
    var configuration = new ApiConfiguration();
    assertThat(configuration.openAPI().getInfo().getVersion()).isEqualTo("1.0.0");
    assertThat(configuration.etagFilter().getUrlPatterns()).containsExactly("/api/*");
  }

  @Test
  void rootDiscoversBooksAndContract() {
    var response = new ApiRootController().root();
    assertThat(response.getBody().getRequiredLink("books").getHref()).isEqualTo("/api/books");
    assertThat(response.getBody().hasLink("describedby")).isTrue();
  }

  @Test
  void errorsUseSafeDetailsAndDisableCaching() {
    var handler = new ApiExceptionHandler();
    var request = new MockHttpServletRequest("GET", "/api/books/42");
    var missing = handler.missing(new BookNotFoundException(42), request);
    assertThat(missing.getStatusCode().value()).isEqualTo(404);
    assertThat(missing.getBody().getInstance().toString()).isEqualTo("/api/books/42");
    assertThat(missing.getHeaders().getCacheControl()).isEqualTo("no-store");
    assertThat(
            handler
                .conflict(new BookConflictException("Already borrowed"), request)
                .getBody()
                .getDetail())
        .isEqualTo("Already borrowed");
    assertThat(
            handler
                .concurrent(new DataIntegrityViolationException("secret SQL"), request)
                .getBody()
                .getDetail())
        .doesNotContain("secret SQL");
  }
}
