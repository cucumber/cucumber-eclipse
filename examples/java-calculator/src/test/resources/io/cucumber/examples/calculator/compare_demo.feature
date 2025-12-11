Feature: Structured Compare Demo
  This feature demonstrates the structured compare functionality
  for Cucumber feature files in Eclipse.

  Background: Common setup
    Given the system is initialized
    And the user is logged in

  Scenario: Simple login test
    Given a user with username "john"
    When the user logs in with password "secret"
    Then the user should see the welcome page
    And the session should be active

  Scenario Outline: Multiple login attempts
    Given a user with username "<username>"
    When the user logs in with password "<password>"
    Then the login should be <result>

    Examples: Valid credentials
      | username | password | result  |
      | john     | secret1  | success |
      | jane     | secret2  | success |

    Examples: Invalid credentials
      | username | password | result  |
      | bob      | wrong    | failure |
      | alice    | invalid  | failure |

  Rule: Password policy enforcement

    Background: Password requirements
      Given password minimum length is 8 characters
      And password must contain at least one number

    Scenario: Valid password
      When a user sets password "secure123"
      Then the password should be accepted

    Scenario: Invalid password - too short
      When a user sets password "short"
      Then the password should be rejected
      And an error message should be displayed

  Scenario: Data table example
    Given the following users:
      | username | email           | role  |
      | john     | john@email.com  | admin |
      | jane     | jane@email.com  | user  |
      | bob      | bob@email.com   | user  |
    When I query all users
    Then I should see 3 users
    And the admin user should be "john"
