package io.github.amoez2005.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the Shelf REST API and its embedded web server. */
@SpringBootApplication
public class LibraryApplication {
  /** Starts the application with the supplied command-line arguments. */
  public static void main(String[] args) {
    SpringApplication.run(LibraryApplication.class, args);
  }
}
