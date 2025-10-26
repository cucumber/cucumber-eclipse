# Cucumber Eclipse - Copilot Instructions

This document provides guidance for GitHub Copilot when working on the Cucumber Eclipse plugin project.

## Project Overview

Cucumber Eclipse is an Eclipse plugin that provides IDE support for Cucumber feature files and integrates with various backend implementations (Java/JVM, Python/Behave, etc.). The project uses:

- **Eclipse PDE** (Plugin Development Environment)
- **Maven/Tycho** for building Eclipse plugins
- **OSGi** for modularity and service discovery
- **Java 21** as the minimum runtime requirement

## Architecture

The project is organized into multiple Eclipse plugin bundles:

- **io.cucumber.eclipse.editor** - Core editor functionality for `.feature` files
- **io.cucumber.eclipse.java** - Java/JVM backend integration
- **io.cucumber.eclipse.python** - Python/Behave backend integration
- **io.cucumber.eclipse.java.plugins** - Plugin extensions for Java backend
- **io.cucumber.eclipse.feature** - Eclipse feature definition
- **io.cucumber.eclipse.product** - Product configuration
- **io.cucumber.eclipse.updatesite** - Update site for distribution

## Creating a New Backend Bundle

This section documents the process of creating a new backend integration (e.g., for a different programming language or test framework), based on the Python/Behave implementation.

### 1. Bundle Structure Setup

Create a new bundle directory with the standard Eclipse plugin structure:

```
io.cucumber.eclipse.<language>/
├── .classpath                    # Eclipse Java classpath
├── .project                      # Eclipse project configuration
├── .settings/                    # Eclipse project settings
│   ├── org.eclipse.jdt.core.prefs
│   └── org.eclipse.pde.ds.annotations.prefs
├── .gitignore                    # Ignore bin/ and build artifacts
├── META-INF/
│   └── MANIFEST.MF              # OSGi bundle metadata
├── build.properties             # PDE build configuration
├── plugin.xml                   # Extension point declarations
├── OSGI-INF/                    # Declarative Services descriptors
├── icons/                       # UI icons (cukes.gif, etc.)
├── src/                         # Java source code
│   └── io/cucumber/eclipse/<language>/
│       ├── Activator.java
│       ├── launching/           # Launch configuration support
│       ├── preferences/         # User preferences
│       ├── steps/              # Step definition support
│       └── validation/         # Glue code validation
├── README.md                    # User documentation
└── IMPLEMENTATION.md           # Technical documentation
```

### 2. Core Components to Implement

#### 2.1 Bundle Activator

Create an `Activator.java` that extends `AbstractUIPlugin`:

```java
public class Activator extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "io.cucumber.eclipse.<language>";
    private static Activator plugin;
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    public static Activator getDefault() {
        return plugin;
    }
}
```

#### 2.2 ILauncher Implementation

Implement `io.cucumber.eclipse.editor.launching.ILauncher` to integrate with the editor's launch framework:

```java
@Component(service = ILauncher.class)
public class <Language>Launcher implements ILauncher {
    
    @Override
    public void launch(Map<GherkinEditorDocument, IStructuredSelection> launchMap, 
                      Mode mode, boolean temporary, IProgressMonitor monitor) {
        // Create and execute launch configurations
    }
    
    @Override
    public boolean supports(IResource resource) {
        // Detect if resource belongs to your language/framework
        return isYourProject(resource);
    }
    
    @Override
    public boolean supports(Mode mode) {
        // Return true for supported modes (RUN, DEBUG)
        return mode == Mode.RUN;
    }
}
```

**Key Points:**
- Register as OSGi service using `@Component` annotation
- Create XML descriptor in `OSGI-INF/`
- Implement project detection in `supports(IResource)`
- Integrate with existing `CucumberFeatureLaunchShortcut`

#### 2.3 Launch Configuration Delegate

Extend `LaunchConfigurationDelegate` to execute your test framework:

```java
public class <Language>LaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, 
                      ILaunch launch, IProgressMonitor monitor) throws CoreException {
        // Read configuration attributes
        // Build command-line arguments
        // Launch the test framework process
        // Attach process to Eclipse debug infrastructure
    }
}
```

**Best Practices:**
- Use a builder pattern for process creation (see `BehaveProcessLauncher`)
- Support variable substitution for paths
- Handle both run and debug modes
- Properly manage process lifecycle

#### 2.4 Process Launcher Builder

Create a builder class to centralize process launching logic:

```java
public class <Framework>ProcessLauncher {
    private String command;
    private String featurePath;
    private String workingDirectory;
    private List<String> additionalArgs = new ArrayList<>();
    
    public <Framework>ProcessLauncher withCommand(String command) {
        this.command = command;
        return this;
    }
    
    // More builder methods...
    
    public Process launch() throws IOException {
        // Build and start the process
    }
    
    public static boolean is<Framework>Project(IResource resource) {
        // Centralized project detection logic
    }
}
```

**Benefits:**
- Eliminates code duplication
- Fluent API for configuration
- Single source of truth for project detection
- Reusable across launch delegate and validation jobs

#### 2.5 Launch Configuration UI

Implement UI tabs for launch configuration:

```java
public class <Language>MainTab extends AbstractLaunchConfigurationTab {
    
    @Override
    public void createControl(Composite parent) {
        // Create UI widgets for configuration
        // Feature path selector
        // Working directory
        // Framework-specific options
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // Save UI values to configuration
    }
    
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        // Load configuration values to UI
    }
}

public class <Language>TabGroup extends AbstractLaunchConfigurationTabGroup {
    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        setTabs(new ILaunchConfigurationTab[] {
            new <Language>MainTab(),
            new CommonTab()
        });
    }
}
```

#### 2.6 Glue Code Validation

Implement background validation to check step definition matching:

##### Document Setup Participant

```java
public class <Language>GlueValidator implements IDocumentSetupParticipant {
    
    @Override
    public void setup(IDocument document) {
        document.addDocumentListener(new IDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                validate(document, 1000); // Delay for debouncing
            }
        });
        validate(document, 0);
    }
    
    private static void validate(IDocument document, int delay) {
        // Schedule background validation job
    }
}
```

Register in `plugin.xml`:
```xml
<extension point="org.eclipse.core.filebuffers.documentSetup">
    <participant
        class="io.cucumber.eclipse.<language>.validation.<Language>GlueValidator"
        contentTypeId="io.cucumber.eclipse.editor.content-type.feature">
    </participant>
</extension>
```

##### Background Validation Job

```java
final class <Language>GlueJob extends Job {
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        // 1. Run framework with dry-run/validation mode
        // 2. Parse output to extract step-to-definition mappings
        // 3. Get all steps from editorDocument.getSteps()
        // 4. Compare matched vs. all steps to find unmatched
        // 5. Create markers for unmatched steps
        
        // Error handling:
        // - Return CANCEL_STATUS for InterruptedException
        // - Return OK_STATUS for errors, create error marker instead
        // - Never return error status (prevents popup in background)
    }
}
```

**Key Points:**
- Execute framework in validation/dry-run mode
- Parse output to extract step mappings
- Use `editorDocument.getSteps()` to get all steps
- Create markers only for truly unmatched steps
- Handle errors gracefully (markers, not popups)

#### 2.7 Step Definition Navigation

Implement `IStepDefinitionOpener` for Ctrl+Click navigation:

```java
@Component(service = IStepDefinitionOpener.class)
public class <Language>StepDefinitionOpener implements IStepDefinitionOpener {
    
    @Override
    public boolean canOpen(IResource resource) {
        // Use centralized project detection
        return <Framework>ProcessLauncher.is<Framework>Project(resource);
    }
    
    @Override
    public boolean openInEditor(ITextViewer textViewer, IResource resource, 
                                Step step) throws CoreException {
        // 1. Get matched steps from validator
        // 2. Find match for current step by line number
        // 3. Open file at specified line
        // 4. Navigate to line in editor
    }
}
```

**Implementation Tips:**
- Register as OSGi service component
- Reuse step mappings from validation job
- Use Eclipse's IDE.openEditor() and text editor APIs
- Handle file path resolution (relative vs. absolute)

#### 2.8 Remote Test Execution (Message Endpoint)

Implement message endpoint for real-time test execution monitoring:

##### Message Endpoint Process

Create a process that receives Cucumber messages from the test framework:

```java
public class <Framework>MessageEndpointProcess implements IProcess, EnvelopeProvider, 
                                                           ISuspendResume, IDisconnect {
    
    private static final int HANDLED_MESSAGE = 0x01;
    private static final int GOOD_BY_MESSAGE = 0x00;
    
    private ServerSocket serverSocket;
    private ILaunch launch;
    private List<Envelope> envelopes = new ArrayList<>();
    private List<EnvelopeListener> consumers = new ArrayList<>();
    
    public <Framework>MessageEndpointProcess(ILaunch launch) throws IOException {
        this.serverSocket = new ServerSocket(0);
        this.launch = launch;
        launch.addProcess(this);
    }
    
    public void start() {
        // Create daemon thread to listen for incoming connections
        // Accept connection and read messages in loop
        // Deserialize using Jackson.OBJECT_MAPPER
        // Notify all registered EnvelopeListeners
        // Send acknowledgment byte after each message
    }
    
    public void addBehaveArguments(List<String> args) {
        // Add framework-specific arguments to inject formatter
        // Include port number for socket connection
    }
}
```

##### Language-Specific Formatter/Plugin

Create a formatter in the target language that sends messages to Eclipse:

**For Python/Behave:**

```python
class CucumberEclipseFormatter(Formatter):
    def __init__(self, stream_opener, config):
        # Read port from environment variable or config
        # Connect to Eclipse socket
        
    def _send_message(self, envelope):
        # Serialize to JSON
        # Send 4-byte big-endian length
        # Send JSON bytes
        # Wait for acknowledgment (0x01)
        
    def step(self, step):
        # Create TestStepFinished envelope
        # Send to Eclipse
        
    def eof(self):
        # Send TestRunFinished
        # Send 0 length
        # Close connection
```

**Protocol Requirements:**

1. Connect to Eclipse on specified port
2. For each message:
   - Send message length as 4-byte big-endian integer
   - Send JSON-encoded Cucumber Message
   - Wait for acknowledgment byte (0x01)
   - If received goodbye (0x00), close connection
3. After test run:
   - Send TestRunFinished message
   - Send 0 length to signal end
   - Wait for acknowledgment
   - Close socket

**Key Points:**
- Use standard Cucumber Messages format (JSON)
- Reuse Jackson ObjectMapper for deserialization (from java.plugins bundle)
- Package formatter/plugin with bundle (e.g., in `<language>-plugins/` directory)
- Update `build.properties` to include plugin directory
- Add formatter to language runtime's plugin path (e.g., PYTHONPATH)
- Pass port via environment variable or command-line argument
- No code changes required in user's test files

##### Update Launch Delegate

Integrate message endpoint with launch configuration:

```java
@Override
public void launch(ILaunchConfiguration config, String mode, 
                  ILaunch launch, IProgressMonitor monitor) throws CoreException {
    
    // Create message endpoint
    <Framework>MessageEndpointProcess endpoint = 
        new <Framework>MessageEndpointProcess(launch);
    
    // Build launcher arguments
    List<String> args = new ArrayList<>();
    endpoint.addArguments(args);  // Adds formatter and port
    
    // Start endpoint listener
    endpoint.start();
    launch.addProcess(endpoint);
    
    try {
        // Launch test process with injected arguments
        Process process = launcher.launch(pluginPath);
        IProcess iProcess = DebugPlugin.newProcess(launch, process, "...");
    } catch (Exception e) {
        endpoint.terminate();
        throw e;
    }
}
```

#### 2.9 Preferences

Provide user-configurable preferences:

##### Preferences Data Class

```java
public class <Language>Preferences {
    public static final String PREF_COMMAND = "<framework>.command";
    public static final String DEFAULT_COMMAND = "<framework-executable>";
    
    public static <Language>Preferences of(IResource resource) {
        // Support both workspace and project-level preferences
    }
    
    public String command() {
        // Return configured command with fallback to default
    }
}
```

##### Preferences Page

```java
public class <Language>PreferencePage extends PreferencePage 
                                        implements IWorkbenchPreferencePage {
    
    @Override
    protected Control createContents(Composite parent) {
        // Create UI for configuration options
        // Command path
        // Additional settings
    }
    
    @Override
    public boolean performOk() {
        // Save preferences
    }
}
```

Register in `plugin.xml`:
```xml
<extension point="org.eclipse.ui.preferencePages">
    <page
        category="cucumber.eclipse.editor.preferences.main"
        class="io.cucumber.eclipse.<language>.preferences.<Language>PreferencePage"
        id="io.cucumber.eclipse.<language>.preferences.page"
        name="<Framework> Backend">
    </page>
</extension>
```

### 3. MANIFEST.MF Configuration

Essential bundle metadata:

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: <Language>
Bundle-SymbolicName: io.cucumber.eclipse.<language>;singleton:=true
Bundle-Version: 3.0.0.qualifier
Bundle-Activator: io.cucumber.eclipse.<language>.Activator
Bundle-RequiredExecutionEnvironment: JavaSE-21
Bundle-ActivationPolicy: lazy
Automatic-Module-Name: io.cucumber.eclipse.<language>

Export-Package: io.cucumber.eclipse.<language>;x-internal:=true,
 io.cucumber.eclipse.<language>.launching;x-internal:=true,
 io.cucumber.eclipse.<language>.preferences;x-internal:=true,
 io.cucumber.eclipse.<language>.steps;x-internal:=true,
 io.cucumber.eclipse.<language>.validation;x-internal:=true

Require-Bundle: org.eclipse.ui,
 org.eclipse.core.runtime,
 io.cucumber.eclipse.editor;bundle-version="1.0.0",
 org.eclipse.jface.text,
 org.eclipse.debug.ui,
 org.eclipse.debug.core,
 org.eclipse.ui.workbench.texteditor,
 org.eclipse.ui.console,
 org.eclipse.ui.ide;bundle-version="3.18.0",
 org.eclipse.core.filebuffers,
 org.eclipse.core.variables,
 io.cucumber.messages;bundle-version="13.2.1",
 io.cucumber.tag-expressions;bundle-version="3.0.0"

Import-Package: org.eclipse.unittest.ui,
 org.osgi.service.component.annotations;version="1.3.0"

Service-Component: OSGI-INF/*.xml
```

### 4. plugin.xml Configuration

Define extension points:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?eclipse version="3.4"?>
<plugin>
    <!-- Launch Configuration Type -->
    <extension point="org.eclipse.debug.core.launchConfigurationTypes">
        <launchConfigurationType
            delegate="io.cucumber.eclipse.<language>.launching.<Language>LaunchConfigurationDelegate"
            id="cucumber.eclipse.<language>.launching.local<Framework>"
            modes="run,debug"
            name="Cucumber-<Framework>"
            public="true">
        </launchConfigurationType>
    </extension>
    
    <!-- Launch Configuration UI -->
    <extension point="org.eclipse.debug.ui.launchConfigurationTypeImages">
        <launchConfigurationTypeImage
            icon="icons/cukes.gif"
            configTypeID="cucumber.eclipse.<language>.launching.local<Framework>"
            id="cucumber.eclipse.<language>.launching.image">
        </launchConfigurationTypeImage>
    </extension>
    
    <extension point="org.eclipse.debug.ui.launchConfigurationTabGroups">
        <launchConfigurationTabGroup
            type="cucumber.eclipse.<language>.launching.local<Framework>"
            class="io.cucumber.eclipse.<language>.launching.<Language>TabGroup"
            id="cucumber.eclipse.<language>.launching.tabGroup">
        </launchConfigurationTabGroup>
    </extension>
    
    <!-- Document Setup for Validation -->
    <extension point="org.eclipse.core.filebuffers.documentSetup">
        <participant
            class="io.cucumber.eclipse.<language>.validation.<Language>GlueValidator"
            contentTypeId="io.cucumber.eclipse.editor.content-type.feature">
        </participant>
    </extension>
    
    <!-- Preferences Page -->
    <extension point="org.eclipse.ui.preferencePages">
        <page
            category="cucumber.eclipse.editor.preferences.main"
            class="io.cucumber.eclipse.<language>.preferences.<Language>PreferencePage"
            id="io.cucumber.eclipse.<language>.preferences.page"
            name="<Framework> Backend">
        </page>
    </extension>
</plugin>
```

### 5. build.properties Configuration

```properties
source.. = src/
output.. = bin/
bin.includes = META-INF/,\
               .,\
               plugin.xml,\
               icons/,\
               OSGI-INF/
```

### 6. Integration with Build System

#### Add to parent pom.xml

```xml
<modules>
    <!-- ... existing modules ... -->
    <module>io.cucumber.eclipse.<language></module>
</modules>
```

#### Add to feature.xml

```xml
<plugin
    id="io.cucumber.eclipse.<language>"
    download-size="0"
    install-size="0"
    version="0.0.0"
    unpack="false"/>
```

### 7. Project Detection Best Practices

Implement hierarchical detection (most specific to most general):

1. **Framework Convention**: Check for framework-specific file structure
   - Example: `.feature` file with adjacent `steps/` directory containing implementation files
   
2. **Language Nature**: Check for Eclipse project nature
   - Example: PyDev nature for Python projects
   
3. **Project Indicators**: Fallback to common project files
   - Example: `requirements.txt`, `setup.py`, `pyproject.toml` for Python

```java
public static boolean is<Framework>Project(IResource resource) {
    // 1. Check framework convention (most specific)
    if (resource.getType() == IResource.FILE && resource.getName().endsWith(".feature")) {
        // Check for framework-specific structure
    }
    
    // 2. Check project nature
    IProject project = resource.getProject();
    try {
        if (project.hasNature("<language>.nature")) {
            return true;
        }
    } catch (CoreException e) {
        // Ignore
    }
    
    // 3. Check project indicators (fallback)
    return project.getFile("project-file").exists();
}
```

### 8. Error Handling Guidelines

#### Background Jobs

- **Never** return error status from background jobs (causes popups)
- Return `Status.OK_STATUS` and create error markers instead
- Return `Status.CANCEL_STATUS` for `InterruptedException`
- Log errors for troubleshooting

```java
try {
    // Validation logic
} catch (InterruptedException e) {
    return Status.CANCEL_STATUS;
} catch (IOException e) {
    ILog.get().error("Validation failed", e);
    try {
        createErrorMarker(resource, "Helpful error message");
    } catch (CoreException ce) {
        // Ignore marker creation failure
    }
    return Status.OK_STATUS;
}
```

#### Marker Management

- Delete existing markers before creating new ones
- Use unique source IDs to identify marker ownership
- Provide actionable error messages
- Link to preferences or logs for more information

### 9. Testing Your Implementation

Create an example project in `examples/<language>-<example>/`:

```
examples/<language>-<example>/
├── .gitignore
├── README.md              # Setup instructions
├── features/
│   ├── <example>.feature  # Feature file
│   └── steps/
│       └── <example>_steps.<ext>  # Step definitions
```

**Testing Checklist:**
- [ ] Launch configuration creates successfully
- [ ] Feature file executes with framework
- [ ] Tags filter scenarios correctly
- [ ] Validation detects unmatched steps
- [ ] Markers appear for unmatched steps
- [ ] Ctrl+Click navigates to step definitions
- [ ] Preferences page saves settings
- [ ] Custom command path works
- [ ] Error markers appear on validation failure
- [ ] Background validation doesn't cause popups

### 10. Documentation

Create two documentation files:

#### README.md (User-facing)
- Installation instructions
- Usage guide
- Configuration options
- Example project walkthrough

#### IMPLEMENTATION.md (Developer-facing)
- Architecture overview
- Component descriptions
- Design decisions
- Extension points
- Future enhancements

## Code Style Guidelines

- **Formatting**: Follow Eclipse Java code conventions
- **Naming**: Use descriptive names, avoid abbreviations
- **Comments**: Document public APIs, explain non-obvious logic
- **Error Messages**: Be specific and actionable
- **Logging**: Use `ILog.get()` for error logging
- **Dependencies**: Mark optional dependencies in MANIFEST.MF

## OSGi Declarative Services

Register services using annotations and XML descriptors:

```java
@Component(service = ILauncher.class)
public class MyLauncher implements ILauncher {
    // Implementation
}
```

Create descriptor in `OSGI-INF/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.3.0" 
               name="io.cucumber.eclipse.<language>.MyLauncher">
    <service>
        <provide interface="io.cucumber.eclipse.editor.launching.ILauncher"/>
    </service>
    <implementation class="io.cucumber.eclipse.<language>.MyLauncher"/>
</scr:component>
```

## Common Pitfalls

1. **Don't create custom launch shortcuts** - Implement `ILauncher` instead
2. **Don't return error status from background jobs** - Create markers
3. **Don't duplicate project detection** - Centralize in one method
4. **Don't hardcode paths** - Support variable substitution
5. **Don't ignore existing markers** - Delete before creating new ones
6. **Don't use snippets if not generated** - Use line numbers only
7. **Don't forget optional dependencies** - Mark with `resolution:=optional`

## Resources

- [Eclipse PDE Guide](https://www.eclipse.org/pde/)
- [OSGi Declarative Services](https://www.osgi.org/developer/architecture/)
- [Eclipse Debug Framework](https://www.eclipse.org/articles/Article-Debugger/how-to.html)
- [Existing Java Implementation](io.cucumber.eclipse.java/)
- [Existing Python Implementation](io.cucumber.eclipse.python/)

## Getting Help

- Check existing implementations (Java, Python bundles)
- Review Eclipse PDE documentation
- Test incrementally and validate each component
- Use the example projects for testing
