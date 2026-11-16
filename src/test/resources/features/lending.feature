Feature: Manage the library through its public REST API
  As a librarian I want safe and discoverable lending actions
  so that available books and active loans stay consistent.

  Scenario: Borrow and return a book by following available actions
    Given an available book in the library
    When I borrow the book
    Then the response status is 200
    And the book offers a "return" link
    When I return the book
    Then the response status is 200
    And the book offers a "borrow" link

  Scenario: Prevent double borrowing
    Given an available book in the library
    When I borrow the book
    And I borrow the book
    Then the response status is 409

  Scenario: Protect a book with an active loan
    Given an available book in the library
    When I borrow the book
    And I delete the book
    Then the response status is 409

  Scenario: Remove a book after its loan is returned
    Given an available book in the library
    When I borrow the book
    And I return the book
    And I delete the book
    Then the response status is 204
    And the book no longer exists
