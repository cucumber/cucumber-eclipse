# Python Calculator Example

This is a simple example demonstrating how to use the Cucumber-Behave launcher with Eclipse.

## Prerequisites

1. Python 3.x installed
2. Behave package installed:
   ```bash
   pip install behave
   ```

## Running the Example

### From Command Line

```bash
cd examples/python-calculator
behave
```

### From Eclipse

1. Open Eclipse with Cucumber Eclipse plugin installed
2. Import this project into Eclipse
3. Right-click on `features/calculator.feature`
4. Select "Run As" > "Cucumber-Behave"
5. In the launch configuration dialog:
   - **Feature Path**: Select the `calculator.feature` file
   - **Working Directory**: Set to the `examples/python-calculator` directory
   - **Python Interpreter**: Use `python` or `python3` depending on your system
   - Click "Run"

## Project Structure

```
python-calculator/
├── features/
│   ├── calculator.feature    # Feature file with scenarios
│   └── steps/
│       └── calculator_steps.py  # Step definitions
└── README.md
```

## Expected Output

When you run the tests, you should see output indicating that all three scenarios pass:

```
Feature: Calculator # features/calculator.feature:1

  Scenario: Add two numbers          # features/calculator.feature:6
    Given I have a calculator         # features/steps/calculator_steps.py:19
    When I add 2 and 3                # features/steps/calculator_steps.py:23
    Then the result should be 5       # features/steps/calculator_steps.py:35

  Scenario: Subtract two numbers     # features/calculator.feature:11
    Given I have a calculator         # features/steps/calculator_steps.py:19
    When I subtract 3 from 5          # features/steps/calculator_steps.py:27
    Then the result should be 2       # features/steps/calculator_steps.py:35

  Scenario: Multiply two numbers     # features/calculator.feature:16
    Given I have a calculator         # features/steps/calculator_steps.py:19
    When I multiply 2 by 3            # features/steps/calculator_steps.py:31
    Then the result should be 6       # features/steps/calculator_steps.py:35

3 scenarios (3 passed)
9 steps (9 passed)
```
