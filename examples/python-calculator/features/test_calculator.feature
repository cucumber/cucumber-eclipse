Feature: Calculator
  As a user
  I want to use a calculator
  So that I can perform basic arithmetic operations

  Scenario: Add two numbers
    Given I have a calculator
    When I add 2 and 3 numbers
    Then the result should be 5

  Scenario: Subtract two numbers
    Given I have a calculatorx
    When I subtract 3 from 5
    Then the result should be 2

  Scenario: Multiply two numbers
    Given I have a calculatorrrr
    When I multiply 2 by 3
    Then the result should be 6
