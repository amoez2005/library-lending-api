package io.github.amoez2005.library.api;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.amoez2005.library.book.BookRepository;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiIntegrationTest {
  @LocalServerPort int port;
  @Autowired BookRepository repository;
  @Autowired ObjectMapper mapper;
  final HttpClient client = HttpClient.newHttpClient();
  final String payload =
      "{\"title\":\"Clean Code\",\"author\":\"Robert Martin\",\"isbn\":\"9780132350884\"}";

  @BeforeEach
  void reset() {
    repository.deleteAll();
  }

  @Test
  void completeLifecycleUsesRealHttpAndDatabase() throws Exception {
    var created = send("POST", "/api/books", payload);
    assertThat(created.statusCode()).isEqualTo(201);
    String location = created.headers().firstValue("Location").orElseThrow();
    var body = mapper.readTree(created.body());
    assertThat(body.at("/_links/borrow/type").asText()).isEqualTo("application/json");
    assertThat(created.headers().firstValue("Set-Cookie")).isEmpty();
    String path = URI.create(location).getPath();
    assertThat(send("PUT", path, payload).statusCode()).isEqualTo(200);
    var borrowed = send("POST", path + "/borrow", "");
    assertThat(mapper.readTree(borrowed.body()).at("/_links/return/href").asText())
        .endsWith("/return");
    assertThat(send("POST", path + "/borrow", "").statusCode()).isEqualTo(409);
    assertThat(send("DELETE", path, "").statusCode()).isEqualTo(409);
    assertThat(send("POST", path + "/return", "").statusCode()).isEqualTo(200);
    assertThat(send("POST", path + "/return", "").statusCode()).isEqualTo(409);
    assertThat(send("DELETE", path, "").statusCode()).isEqualTo(204);
    assertThat(send("GET", path, "").statusCode()).isEqualTo(404);
  }

  @Test
  void cacheRevalidationChangesAfterMutation() throws Exception {
    String path =
        URI.create(
                send("POST", "/api/books", payload).headers().firstValue("Location").orElseThrow())
            .getPath();
    var original = send("GET", path, "");
    String etag = original.headers().firstValue("ETag").orElseThrow();
    assertThat(original.headers().firstValue("Cache-Control")).hasValue("no-cache");
    var conditional =
        HttpRequest.newBuilder(URI.create(base() + path))
            .header("If-None-Match", etag)
            .GET()
            .build();
    assertThat(client.send(conditional, HttpResponse.BodyHandlers.ofString()).statusCode())
        .isEqualTo(304);
    send("POST", path + "/borrow", "");
    var changed = client.send(conditional, HttpResponse.BodyHandlers.ofString());
    assertThat(changed.statusCode()).isEqualTo(200);
    assertThat(changed.headers().firstValue("ETag").orElseThrow()).isNotEqualTo(etag);
  }

  @Test
  void validatesInputsAndReportsProblemDetails() throws Exception {
    for (String invalid :
        List.of(
            "{}", payload.replace("Clean Code", "   "), payload.replace("9780132350884", "bad"))) {
      var response = send("POST", "/api/books", invalid);
      assertThat(response.statusCode()).isEqualTo(400);
      assertThat(response.headers().firstValue("Content-Type").orElse(""))
          .contains("application/problem+json");
      assertThat(mapper.readTree(response.body()).get("errors").size()).isGreaterThan(0);
    }
    assertThat(send("POST", "/api/books", "{").statusCode()).isEqualTo(400);
    for (String query :
        List.of("page=-1", "size=0", "size=101", "page=abc", "q=" + "a".repeat(161))) {
      assertThat(send("GET", "/api/books?" + query, "").statusCode()).isEqualTo(400);
    }
    send("POST", "/api/books", payload);
    assertThat(send("POST", "/api/books", payload).statusCode()).isEqualTo(409);
  }

  @Test
  void searchesPaginatesAndDiscoversApi() throws Exception {
    send("POST", "/api/books", payload);
    send(
        "POST",
        "/api/books",
        payload
            .replace("Clean Code", "Clean Architecture")
            .replace("9780132350884", "9780134494166"));
    JsonNode page = mapper.readTree(send("GET", "/api/books?q=clean&size=1", "").body());
    assertThat(page.get("totalElements").asInt()).isEqualTo(2);
    assertThat(page.at("/_links/next/href").asText()).contains("page=1");
    JsonNode second = mapper.readTree(send("GET", "/api/books?q=clean&size=1&page=1", "").body());
    assertThat(second.at("/_links/prev/href").asText()).contains("page=0");
    assertThat(mapper.readTree(send("GET", "/api/books?q=missing", "").body()).get("items"))
        .isEmpty();
    assertThat(mapper.readTree(send("GET", "/api", "").body()).at("/_links/books/href").asText())
        .isEqualTo("/api/books");
    assertThat(send("GET", "/v3/api-docs", "").statusCode()).isEqualTo(200);
    assertThat(send("GET", "/", "").body()).contains("Library desk");
  }

  @Test
  void swaggerEditorCanMakeCrossOriginRequests() throws Exception {
    var request =
        HttpRequest.newBuilder(URI.create(base() + "/api/books"))
            .header("Origin", "https://editor.swagger.io")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "content-type")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();
    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
        .hasValue("https://editor.swagger.io");
  }

  @Test
  void concurrentDuplicateCreationHasOnlyOneWinner() throws Exception {
    var request =
        HttpRequest.newBuilder(URI.create(base() + "/api/books"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
    var first = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    var second = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    CompletableFuture.allOf(first, second).join();
    assertThat(List.of(first.join().statusCode(), second.join().statusCode()))
        .containsExactlyInAnyOrder(201, 409);
    assertThat(repository.count()).isEqualTo(1);
  }

  private String base() {
    return "http://localhost:" + port;
  }

  private HttpResponse<String> send(String method, String path, String body) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create(base() + path))
            .header("Content-Type", "application/json")
            .method(method, HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
