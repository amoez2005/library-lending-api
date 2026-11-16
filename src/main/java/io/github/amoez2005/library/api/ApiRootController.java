package io.github.amoez2005.library.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Provides the API's hypermedia entry point. */
@RestController
public class ApiRootController {
  /** Starts resource discovery without client-side URI construction. */
  @GetMapping("/api")
  public ResponseEntity<RepresentationModel<?>> root() {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .body(
            new RepresentationModel<>()
                .add(
                    Link.of("/api").withSelfRel(),
                    Link.of("/api/books").withRel("books"),
                    Link.of("/v3/api-docs").withRel("describedby")));
  }
}
