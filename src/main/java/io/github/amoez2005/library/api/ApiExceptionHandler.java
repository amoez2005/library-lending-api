package io.github.amoez2005.library.api;

import io.github.amoez2005.library.book.BookConflictException;
import io.github.amoez2005.library.book.BookNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** Maps domain and validation failures to safe RFC 9457 problem details. */
@RestControllerAdvice
public class ApiExceptionHandler {
  /** Reports a missing resource. */
  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<ProblemDetail> missing(
      BookNotFoundException error, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, error.getMessage(), request);
  }

  /** Reports an invalid state transition. */
  @ExceptionHandler(BookConflictException.class)
  public ResponseEntity<ProblemDetail> conflict(
      BookConflictException error, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, error.getMessage(), request);
  }

  /** Hides SQL details from concurrent uniqueness and optimistic lock failures. */
  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class
  })
  public ResponseEntity<ProblemDetail> concurrent(
      RuntimeException error, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT, "Resource changed or ISBN already exists. Reload and retry.", request);
  }

  /** Reports individual invalid request fields. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> invalid(
      MethodArgumentNotValidException error, HttpServletRequest request) {
    var response = problem(HttpStatus.BAD_REQUEST, "Request validation failed.", request);
    response
        .getBody()
        .setProperty(
            "errors",
            error.getBindingResult().getFieldErrors().stream()
                .map(
                    field ->
                        Map.of(
                            "field",
                            field.getField(),
                            "message",
                            String.valueOf(field.getDefaultMessage())))
                .toList());
    return response;
  }

  /** Rejects query parameters outside documented bounds. */
  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ProblemDetail> query(
      HandlerMethodValidationException error, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST, "Query parameters are outside the allowed bounds.", request);
  }

  private ResponseEntity<ProblemDetail> problem(
      HttpStatus status, String message, HttpServletRequest request) {
    ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
    detail.setInstance(URI.create(request.getRequestURI()));
    return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(detail);
  }
}
