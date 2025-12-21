# Behave Cucumber Eclipse Plugin

This directory contains Python plugins that integrate Behave with Eclipse IDE.

## behave_cucumber_eclipse.py

A Behave formatter that enables real-time test execution monitoring in Eclipse.

### Features

- Sends test execution events to Eclipse via socket connection
- Uses the Cucumber Messages protocol
- Compatible with Eclipse unittest view
- No code changes required in test files

### Installation

1. Copy `behave_cucumber_eclipse.py` to your Python path or project directory
2. The plugin is automatically injected by Eclipse when launching tests

### Manual Usage

If you want to use the formatter outside of Eclipse:

```bash
# Set the port number via environment variable
export CUCUMBER_ECLIPSE_PORT=12345

# Run behave with the formatter
behave --format behave_cucumber_eclipse:CucumberEclipseFormatter features/
```

Or pass the port via userdata:

```bash
behave -D cucumber_eclipse_port=12345 --format behave_cucumber_eclipse:CucumberEclipseFormatter features/
```

### Protocol

The formatter implements the same socket protocol as the Java CucumberEclipsePlugin:

1. Connect to Eclipse on the specified port
2. For each message:
   - Send message length as 4-byte big-endian integer
   - Send JSON-encoded Cucumber message
   - Wait for acknowledgment byte (0x01)
3. After TestRunFinished, send 0 length and wait for goodbye byte (0x00)

### Dependencies

- Python 3.6+
- behave
- Standard library only (socket, json, struct)

No additional dependencies required - the plugin uses only Python standard library for socket communication and JSON serialization.
