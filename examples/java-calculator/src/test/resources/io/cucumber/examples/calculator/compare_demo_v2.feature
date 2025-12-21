Feature: Structured Compare Demo
  This feature demonstrates the structured compare functionality
  for Cucumber feature files in Eclipse.
  Updated version with modifications for comparison.

  Background: Common setup
    Given the system is initialized
    And the user is authenticated
    And the database is clean

  Scenario: Simple login test
    Given a user with username "john"
    When the user logs in with password "secret"
    Then the user should see the dashboard
    And the session should be active
    And notifications should be loaded

  Scenario: Quick logout test
    Given a logged in user
    When the user clicks logout
    Then the user should be logged out

  Scenario Outline: Multiple login attempts
    Given a user with username "<username>"
    When the user logs in with password "<password>"
    Then the login should be <result>
    And audit log should record the attempt

    Examples: Valid credentials
      | username | password | result  |
      | john     | secret1  | success |
      | jane     | secret2  | success |
      | admin    | admin123 | success |

    Examples: Invalid credentials
      | username | password | result  |
      | bob      | wrong    | failure |

  Rule: Password policy enforcement

    Background: Password requirements
      Given password minimum length is 10 characters
      And password must contain at least one number
      And password must contain at least one special character

    Scenario: Valid password
      When a user sets password "secure@123"
      Then the password should be accepted
      And a confirmation email should be sent

    Scenario: Invalid password - too short
      When a user sets password "short"
      Then the password should be rejected
      And an error message should be displayed
      And the user should be prompted to try again

  Scenario: Data table example
    Given the following users:
      | username | email           | role       |
      | john     | john@email.com  | admin      |
      | jane     | jane@email.com  | user       |
      | bob      | bob@email.com   | user       |
      | alice    | alice@email.com | moderator  |
    When I query all users
    Then I should see 4 users
    And the admin user should be "john"
    And the moderator user should be "alice"
