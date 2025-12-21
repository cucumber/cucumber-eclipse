# Implementation Summary: io.cucumber.eclipse.python Bundle

This document describes the implementation of the new `io.cucumber.eclipse.python` bundle that enables launching Cucumber feature files using Python's Behave framework.

## Implementation Overview

The bundle provides a complete Eclipse launch configuration for running Cucumber feature files with Behave, following the same architectural pattern as the existing `io.cucumber.eclipse.java` bundle.

## Components Implemented

### 1. Core Bundle Configuration

- **MANIFEST.MF**: Defines bundle metadata, dependencies, and exported packages
  - Dependencies include Eclipse UI/Debug framework and optional PyDev support
  - Targets JavaSE-21 runtime environment
  - Optional resolution for PyDev plugins to avoid hard dependency

- **plugin.xml**: Declares Eclipse extension points
  - Launch configuration type: `cucumber.eclipse.python.launching.localCucumberBehave`
  - Launch configuration UI (tab groups and icon)
  - Launch shortcut for context menu integration
  - Contextual launch support for `.feature` files

- **build.properties**: Maven/Tycho build configuration
  - Source directory: `src/`
  - Binary directory: `bin/`
  - Includes plugin.xml, icons, and OSGI-INF

### 2. Launch Framework Classes

#### CucumberBehaveLaunchConstants
- Defines configuration attribute keys:
  - `ATTR_FEATURE_PATH`: Path to feature file
  - `ATTR_WORKING_DIRECTORY`: Working directory for execution
  - `ATTR_PYTHON_INTERPRETER`: Python interpreter path
  - `ATTR_TAGS`: Tag expression for filtering
  - `ATTR_IS_VERBOSE`: Enable verbose output
  - `ATTR_IS_NO_CAPTURE`: Disable output capture
  - `ATTR_IS_DRY_RUN`: Enable dry-run mode

#### CucumberBehaveLaunchConfigurationDelegate
- Main launch delegate extending `LaunchConfigurationDelegate`
- Responsibilities:
  - Read launch configuration attributes
  - Build behave command with appropriate flags
  - Create and manage Python process
  - Handle working directory and environment
  - Support for tags, verbose, no-capture, and dry-run options

#### CucumberBehaveTabGroup
- Launch configuration tab group
- Includes:
  - CucumberBehaveMainTab (main configuration)
  - EnvironmentTab (environment variables)
  - CommonTab (common launch settings)

#### CucumberBehaveMainTab
- Main configuration UI tab extending `AbstractLaunchConfigurationTab`
- UI Components:
  - Feature Path selector with file browser
  - Working Directory selector with directory browser
  - Python Interpreter text field
  - Tags text field
  - Behave Options checkboxes (Verbose, No Capture, Dry Run)
- Validates required fields (feature path, working directory)

#### CucumberBehaveLauncher
- Implements `ILauncher` interface for integration with editor's launch framework
- Registered as OSGi service component
- Automatically discovered by `CucumberFeatureLaunchShortcut` in editor bundle
- Supports running feature files and specific scenarios
- Handles tag filtering and temporary launch configurations

### 3. Bundle Infrastructure

- **Activator.java**: OSGi bundle activator
- **OSGI-INF/CucumberBehaveLauncher.xml**: OSGi Declarative Services descriptor for ILauncher registration
- **.project**: Eclipse project configuration (PDE plugin nature)
- **.classpath**: Java classpath configuration (JavaSE-21)
- **.settings/**: Eclipse project settings (JDT, PDE)
- **.gitignore**: Excludes bin/ directory from version control

## Integration with Existing Codebase

### Parent POM
- Added `io.cucumber.eclipse.python` module to parent `pom.xml`

### Feature Definition
- Added plugin entry to `io.cucumber.eclipse.feature/feature.xml`

## Example Project

Created `examples/python-calculator/` demonstrating usage:
- Simple calculator feature with scenarios
- Python step definitions using Behave
- README with setup and usage instructions
- Demonstrates add, subtract, and multiply operations

## Design Decisions

1. **Independent Bundle**: Created as a standalone bundle rather than extending existing Java bundle
   - Cleaner separation of concerns
   - Easier to maintain and update independently
   - Follows Eclipse plugin architecture best practices

2. **ILauncher Implementation**: Implements the `ILauncher` interface from editor bundle
   - Integrates with existing `CucumberFeatureLaunchShortcut` in editor
   - Registered as OSGi service component for automatic discovery
   - Supports running from editor or project explorer context menus
   - No need for custom launch shortcut implementation

3. **Optional PyDev Dependencies**: Marked as optional in MANIFEST.MF
   - Allows bundle to work without PyDev installed
   - Provides better integration when PyDev is available
   - Future enhancement: Could use PyDev for Python interpreter selection

4. **Simple Launch Delegate**: Uses standard ProcessBuilder
   - Direct execution of behave command
   - No dependency on PyDev launch infrastructure
   - Easy to understand and maintain
   - Future enhancement: Could integrate with PyDev's Python runner

5. **Behave-specific Options**: Focused on common Behave options
   - Verbose, no-capture, and dry-run flags
   - Tag filtering support
   - Future enhancement: Could add more Behave-specific options (format, color, etc.)

## Future Enhancements

1. **PyDev Integration**
   - Use PyDev's Python interpreter configuration
   - Integrate with PyDev's Python project settings
   - Use PyDev's console for output

2. **Debug Support**
   - Integrate with Python debugger
   - Breakpoint support in step definitions
   - Variable inspection

3. **Advanced Behave Options**
   - More formatter options (JSON, JUnit XML, etc.)
   - Coverage integration
   - Parallel execution support

4. **Step Definition Navigation**
   - Jump from feature file to step definition ✅ (Implemented)
   - Step definition completion
   - Unused step detection

5. **Test Results Integration** ✅ (Implemented)
   - Eclipse test results view integration
   - Visual representation of test execution
   - Failed test navigation

## Recent Updates

### Remote Test Execution Support (v3.0.0)

Added support for real-time test execution monitoring using the Cucumber Messages protocol:

#### New Components

1. **BehaveMessageEndpointProcess**
   - Located in `io.cucumber.eclipse.python.launching`
   - Implements message endpoint for receiving Cucumber messages from Python
   - Similar to Java's MessageEndpointProcess but tailored for Python/Behave
   - Implements EnvelopeProvider interface for integration with Eclipse
   - Creates server socket and listens for incoming test execution messages
   - Handles message deserialization using Jackson ObjectMapper
   - Distributes messages to registered EnvelopeListeners

2. **Python Formatter Plugin**
   - Located in `python-plugins/behave_cucumber_eclipse.py`
   - Custom Behave formatter that connects to Eclipse via socket
   - Sends Cucumber Messages in JSON format
   - Uses same protocol as Java CucumberEclipsePlugin:
     - 4-byte big-endian integer for message length
     - JSON-encoded message
     - Waits for acknowledgment byte (0x01)
     - Sends goodbye message (0x00) on completion
   - Reads port from environment variable or Behave userdata (-D option)
   - No additional Python dependencies beyond standard library

3. **Updated Launch Configuration**
   - `CucumberBehaveLaunchConfigurationDelegate` now creates MessageEndpointProcess
   - Automatically injects formatter arguments to Behave command
   - Adds python-plugins directory to PYTHONPATH
   - Coordinates between Eclipse and Python processes

4. **Enhanced BehaveProcessLauncher**
   - Added support for setting PYTHONPATH environment variable
   - New `launch(String pythonPluginPath)` method for custom environment setup
   - Maintains backward compatibility with existing `launch()` method

#### Protocol Details

The Python formatter implements the same binary protocol as the Java backend:

```
1. Connect to Eclipse on specified port (passed via -D cucumber_eclipse_port=<port>)
2. For each test event:
   a. Serialize event as Cucumber Message (JSON)
   b. Send message length as 4-byte big-endian integer
   c. Send JSON message bytes
   d. Wait for acknowledgment (0x01) from Eclipse
   e. If Eclipse sends goodbye (0x00), close connection
3. After test run completes:
   a. Send TestRunFinished message
   b. Send 0 length to signal end
   c. Wait for final acknowledgment
   d. Close socket
```

#### Integration Points

- **EnvelopeProvider**: MessageEndpointProcess implements this interface to distribute messages
- **EnvelopeListener**: Eclipse components can register as listeners to receive test events
- **Unittest View**: Messages are routed to Eclipse's unittest view for display
- **Jackson**: Reuses Jackson ObjectMapper from java.plugins bundle for JSON deserialization

#### Build Configuration

- Updated `build.properties` to include python-plugins directory in bundle
- Updated `MANIFEST.MF` to add dependency on io.cucumber.eclipse.java.plugins
- Python formatter is packaged with the plugin and added to PYTHONPATH automatically


## Testing

The implementation has been designed to follow the same patterns as the Java bundle, but requires a full Eclipse environment with the plugin installed to test:

1. Import the bundle into Eclipse with PDE
2. Launch Eclipse Application (Run As > Eclipse Application)
3. Create a Python project with Behave installed
4. Create a feature file
5. Right-click and select "Run As > Cucumber-Behave"

## Compliance with Requirements

✅ New bundle `io.cucumber.eclipse.python` created
✅ Contains methods to launch Python process using Behave
✅ Reuses Eclipse Debug framework (similar to PyDev approach)
✅ Executes Cucumber feature files with Python glue code via Behave
✅ Defines `org.eclipse.debug.core.launchConfigurationTypes` as "Cucumber-Behave"
✅ Follows the Java implementation (`io.cucumber.eclipse.java`) as a blueprint
✅ Launch configuration UI with tabs for configuration
✅ Implements `ILauncher` interface for integration with editor's launch framework
✅ Registered as OSGi service component for automatic discovery
