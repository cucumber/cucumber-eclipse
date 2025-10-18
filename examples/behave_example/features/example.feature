Feature: Basic arithmetic operations
  A simple example that verifies incrementing a number.

  Scenario: Increment a number
    Given a number 1
    When I increment the number by 1
    Then the result should be 2