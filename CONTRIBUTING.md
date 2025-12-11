# Contributing to Cucumber Eclipse

Thank you for your interest in contributing to Cucumber Eclipse! This document provides guidelines and instructions for setting up your development environment and contributing to the project.

## Setting Up Your Development Environment

### Quick Setup with Oomph (Recommended)

The easiest way to get started is using the Oomph setup file:

1. Download and run the [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer)
2. Switch to "Advanced Mode" (menu icon in top-right)
3. Add the project setup file: `https://raw.githubusercontent.com/laeubi/cucumber-eclipse/main/CucumberEclipse.setup`
4. Follow the wizard to complete the setup

For detailed instructions, see the [README.md](README.md#setting-up-development-environment).

### Manual Setup

If you prefer manual setup:

1. **Clone the repository:**
   ```bash
   git clone git@github.com:cucumber/cucumber-eclipse.git
   cd cucumber-eclipse
   ```

2. **Import all projects into Eclipse:**
   - Use `File` → `Import` → `Existing Projects into Workspace`
   - Select the repository root and enable "Search for nested projects"
   - Import all projects

3. **Set the target platform:**
   - Open `io.cucumber.eclipse.targetdefinition/cucumber.eclipse.targetdefinition.target`
   - Click "Set as Active Target Platform"

4. **Build the workspace:**
   - `Project` → `Clean...` → Clean all projects

## Project Structure

### Plugins

* **io.cucumber.eclipse.editor** - Core editor functionality for `.feature` files, syntax highlighting, and content assist
* **io.cucumber.eclipse.java** - Java/JVM backend integration with step definition detection and navigation
* **io.cucumber.eclipse.java.plugins** - Plugin extensions for Java backend (additional integrations)
* **io.cucumber.eclipse.python** - Python/Behave backend integration
* **io.cucumber.eclipse.feature** - Eclipse feature definition that packages all plugins
* **io.cucumber.eclipse.product** - Product configuration for standalone distribution
* **io.cucumber.eclipse.updatesite** - P2 update site for plugin distribution

### Supporting Modules

* **io.cucumber.eclipse.targetdefinition** - Target platform definition (Eclipse platform and dependencies)
* **examples/** - Example projects demonstrating plugin usage

## Building the Project

### Using Maven (Command Line)

Build all modules:
```bash
mvn clean install
```

The update site will be built in `io.cucumber.eclipse.updatesite/target/repository/`.

### Using Eclipse (IDE)

Eclipse will automatically build the projects when you make changes. To trigger a manual build:
- `Project` → `Clean...` → Clean all projects

## Running and Testing

### Running the Plugin in Development

1. Right-click on any plugin project
2. Select `Run As` → `Eclipse Application`
3. A new Eclipse instance will launch with your plugin installed

### Creating a Launch Configuration

1. Go to `Run` → `Run Configurations...`
2. Create a new "Eclipse Application" configuration
3. Set appropriate VM arguments if needed (e.g., `-Xmx2048m`)
4. Run the configuration

### Running Tests

Tests are run automatically during the Maven build. To run them manually:

```bash
mvn clean verify
```

## Making Changes

### Code Style

- Follow Eclipse Java code conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Keep changes focused and minimal

### Commit Guidelines

- Write clear, descriptive commit messages
- Reference issue numbers in commits (e.g., "Fix #123: Description")
- Keep commits atomic (one logical change per commit)
- Sign-off your commits if required by the project

### Pull Request Process

1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes with appropriate tests
4. Ensure all tests pass
5. Push to your fork and submit a pull request
6. Wait for review and address any feedback

## Requirements and Dependencies

- **Java**: Java 21 or higher (JDK 21)
- **Eclipse**: Eclipse 2024-06 or later (IDE for Eclipse Committers recommended)
- **Maven**: Maven 3.6+ (for command-line builds)
- **Target Platform**: Eclipse 2025-09 (or compatible release)

## Adding Dependencies

When adding dependencies to plugins:

1. Add to the appropriate `MANIFEST.MF` file
2. Ensure compatibility with the target platform
3. Update the target definition if adding external dependencies
4. Document the dependency requirement

## Resources

- [Eclipse Plugin Development](https://www.eclipse.org/pde/)
- [Tycho Maven Plugin](https://www.eclipse.org/tycho/)
- [Cucumber Documentation](https://cucumber.io/docs)
- [Project Wiki](https://github.com/cucumber/cucumber-eclipse/wiki)

## Getting Help

- Check existing issues on GitHub
- Review the project wiki
- Ask questions in pull requests or issues
- Join community discussions

## License

By contributing to Cucumber Eclipse, you agree that your contributions will be licensed under the project's license.
