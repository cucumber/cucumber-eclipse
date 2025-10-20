# Cucumber Eclipse Python Bundle

This bundle provides support for launching Cucumber feature files using Python's Behave framework.

## Features

- Launch Cucumber feature files with Behave
- Configure Python interpreter path
- Set working directory for test execution
- Support for Behave command-line options:
  - Verbose output
  - No capture mode
  - Dry run
- Tag-based test filtering

## Requirements

- Python 3.x installed on your system
- Behave package installed (`pip install behave`)
- PyDev plugin (optional, but recommended)

## Usage

1. Right-click on a `.feature` file in your project
2. Select "Run As" > "Cucumber-Behave"
3. Configure the launch configuration:
   - **Feature Path**: Path to the feature file to run
   - **Working Directory**: Directory where behave will be executed
   - **Python Interpreter**: Path to Python executable (defaults to `python`)
   - **Tags**: Optional tag expression to filter scenarios
   - **Behave Options**: Enable verbose, no-capture, or dry-run modes

## Configuration

The launch configuration supports the following attributes:

- `ATTR_FEATURE_PATH`: Path to the feature file
- `ATTR_WORKING_DIRECTORY`: Working directory for execution
- `ATTR_PYTHON_INTERPRETER`: Python interpreter path
- `ATTR_TAGS`: Tag expression for filtering
- `ATTR_IS_VERBOSE`: Enable verbose output
- `ATTR_IS_NO_CAPTURE`: Disable output capture
- `ATTR_IS_DRY_RUN`: Run in dry-run mode

## Example Project Structure

```
my-python-project/
├── features/
│   ├── calculator.feature
│   └── steps/
│       └── calculator_steps.py
└── behave.ini
```

## Notes

- Make sure Behave is installed in your Python environment
- The working directory should typically be the root of your project
- PyDev dependencies are optional and marked with `resolution:=optional` in the manifest
