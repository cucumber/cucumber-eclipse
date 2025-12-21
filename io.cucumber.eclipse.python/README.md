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
- **Real-time test execution monitoring** via Eclipse unittest view
- **Remote test execution support** using Cucumber Messages protocol

## Requirements

- Python 3.6+
- Behave package installed (`pip install behave`)
- PyDev plugin (optional, but recommended)

## Usage

1. Right-click on a `.feature` file in your project
2. Select "Run As" > "Cucumber" (or "Cucumber Feature")
3. The Python/Behave launcher will automatically be used for Python projects
4. Configure the launch configuration:
   - **Feature Path**: Path to the feature file to run
   - **Working Directory**: Directory where behave will be executed
   - **Python Interpreter**: Path to Python executable (defaults to `python`)
   - **Tags**: Optional tag expression to filter scenarios
   - **Behave Options**: Enable verbose, no-capture, or dry-run modes

## Remote Test Execution

The bundle automatically integrates with Eclipse's unittest view to provide real-time test execution feedback:

- Test results are streamed from Behave to Eclipse via socket connection
- Uses the Cucumber Messages protocol for standardized communication
- No code changes required in your test files
- The `behave_cucumber_eclipse.py` formatter is automatically injected

### How It Works

1. When you launch a feature file, Eclipse creates a message endpoint listening on a random port
2. The launch configuration automatically adds the Behave formatter and port number
3. The Python formatter connects to Eclipse and sends test execution events
4. Eclipse displays results in the unittest view in real-time

### Python Plugin

The Python formatter plugin is located in `python-plugins/behave_cucumber_eclipse.py` and is automatically added to your PYTHONPATH during test execution.

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
- The remote execution feature uses the same protocol as the Java backend for consistency
