package io.github.amoez2005.library.acceptance;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import io.github.amoez2005.library.book.BookRepository;
import java.net.URI;
import java.net.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LibrarySteps {
  @LocalServerPort int port;
  @Autowired ObjectMapper mapper;
  @Autowired BookRepository repository;
  private final HttpClient client = HttpClient.newHttpClient();
  private HttpResponse<String> response;
  private String location;

  @Given("an available book in the library")
  public void availableBook() throws Exception {
    repository.deleteAll();
    response =
        request(
            "POST",
            "/api/books",
            "{\"title\":\"Acceptance Book\",\"author\":\"QA Team\",\"isbn\":\"9780132350884\"}");
    assertThat(response.statusCode()).isEqualTo(201);
    location = URI.create(response.headers().firstValue("Location").orElseThrow()).getPath();
  }

  @When("I borrow the book")
  public void borrow() throws Exception {
    response = request("POST", location + "/borrow", "");
  }

  @When("I return the book")
  public void returnBook() throws Exception {
    response = request("POST", location + "/return", "");
  }

  @When("I delete the book")
  public void delete() throws Exception {
    response = request("DELETE", location, "");
  }

  @Then("the response status is {int}")
  public void status(int status) {
    assertThat(response.statusCode()).isEqualTo(status);
  }

  @Then("the book offers a {string} link")
  public void link(String relation) throws Exception {
    assertThat(mapper.readTree(response.body()).at("/_links/" + relation + "/href").asText())
        .endsWith("/" + relation);
  }

  @Then("the book no longer exists")
  public void missing() throws Exception {
    assertThat(request("GET", location, "").statusCode()).isEqualTo(404);
  }

  private HttpResponse<String> request(String method, String path, String body) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .method(method, HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
