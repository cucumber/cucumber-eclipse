# Cucumber-Eclipse 3.0

[![Build Status](https://github.com/cucumber/cucumber-eclipse/actions/workflows/maven.yml/badge.svg)](https://github.com/cucumber/cucumber-eclipse/actions/workflows/maven.yml)

An Eclipse plugin for [Cucumber Version 7](https://cucumber.io/).


**🚀Turbocharge Your Testing with Cucumber Eclipse Plugin!💻**

_Where sleek plugin meets seamless BDD workflow_

**🎯 Key Features That Make You Fall in Love:** [Plugin-wiki](https://github.com/cucumber/cucumber-eclipse/wiki)
- ⚡️ One-Click Gherkin Execution: Run feature files instantly—no more command-line gymnastics.
- 🧠 Smart Step Matching: Auto-link steps to definitions like magic. Zero guesswork, full precision.
- 🚀 Auto-suggestions: Context-Aware Autocomplete - Speeds up typing, cuts down typos, feels like IDE sorcery.
- 🗂️ Step definition navigation: Jump between features and step definitions like a time traveler.
- 🔍 Find References: Right-click on step definition methods to see which feature files use them.
- 🎨 I18n Syntax Highlighting: Make your tests shine with vibrant visual feedback.
- 🛠️ Cucumber Console Output Panel: See clear results and trace bugs in a heartbeat.



🧠 Whether you're a QA rockstar or a developer who loves clean code, this plugin brings clarity, speed, and cucumber-fresh productivity to your workflow.

🌱 Grow your features. Nurture your steps. Reap reliable results.

🎉 Elevate Testing. Empower Teams. Embrace Cucumber.

## Eclipse-Marketplace

![Eclipse-Marketplace](https://github.com/cucumber/cucumber-eclipse/blob/gh-pages/images/EclipseMarketPPlace.png)

- Available in [Eclipse-Marketplace](https://marketplace.eclipse.org/content/cucumber-eclipse-plugin).
- Please refer our [Eclipse-Marketplace-Wiki](https://github.com/cucumber/cucumber-eclipse/wiki/Eclipse-Market-Place-For-Cucumber-Eclipse-Plugin) page for detail information.

## Install the Plugin

You can install the latest release using the follwoing update site in Eclipse:

https://cucumber.github.io/cucumber-eclipse/update-site

For users who wants to keep up-to-date with the latest development version please use 

https://cucumber.github.io/cucumber-eclipse/update-site/main

You are welcome to [report any issue](https://github.com/cucumber/cucumber-eclipse/issues).

### Install the plugin Offline

You can also download the whole site as a Zip for Offline install here:

https://github.com/cucumber/cucumber-eclipse/archive/refs/heads/gh-pages.zip

then you need to point Eclipse to the folder of version your are interested in.

## First Steps

- After you install the Cucumber-Eclipse plugin, you can use it to run Cucumber-JVM.
- Create a new feature file from Eclipse by selecting New => File from the menu and naming it with a ".feature" suffix to bring up the Feature Editor. After typing in the Gherkin code for a test, select Run => Run to invoke Cucumber-JVM on that feature. This will also create a run configuration, which you can modify and rename by selecting Run => Run Configurarations.... Tags are not available in Cucumber-Eclipse, but you can organize your features into directories and select the Feature Path that you want the run configuration to use. You can execute run configurations from the Run => Run History menu.
- Another alternative is to use Cucumber-Eclipse for editing feature files and getting the generated step-definition stubs, but then running a Junit file with a @RunWith(cucumber.class) annotation similar to the cucumber-java-skeleton [RunCukesTest.java](https://github.com/cucumber/cucumber-java-skeleton/blob/master/src/test/java/skeleton/RunCukesTest.java). The @CucumberOptions most useful are

* Run the feature or all features below the directory
  ```gherkin
  features = {"featurePath/dir1", "featurePath2/dir/one_more.feature", ...}
  ```

* Run all features with the given tag
  ```gherkin
  tags = {"@tag1", "@tag2", ...}
  ```

* Use the listed formatter
  ```gherkin
  format = "progress"
  ```

* Find the step definition and hooks below the given directory
  ```gherkin
  glue = "my_feature_steps/dir"
  ```

The full option list can be found at [Cucumber-Options](https://github.com/cucumber/cucumber-jvm/blob/master/core/src/main/java/cucumber/api/CucumberOptions.java)

## Automatic Feature File Validation with Project Builder

Cucumber-Eclipse provides an optional project builder that automatically validates all feature files in your project during each build. This feature is useful when you want immediate feedback on step definition matching across your entire project.

### Enabling the Builder

To enable automatic validation for a Java project:

1. Right-click on your project in the Project Explorer
2. Select **Project → Configure → Enable Cucumber Builder**
3. The builder will now validate all `.feature` files whenever the project is built

### Disabling the Builder

To disable automatic validation:

1. Right-click on your project in the Project Explorer
2. Select **Project → Configure → Disable Cucumber Builder**

### Important Notes

- The builder validates **all** feature files in the project, not just those currently open in an editor
- This may be slower than on-demand validation (which only validates files as you open them)
- Validation runs automatically on every build, including when Java files change (since step definitions may be affected)
- For large projects with many feature files, consider whether the automatic validation overhead is acceptable for your workflow
- If you only work with a subset of feature files at a time, you may prefer to keep the builder disabled and rely on on-demand validation as you open files in the editor

## Customizing Cucumber Features with Activity Groups

Cucumber Eclipse supports Eclipse Activity Groups, which allow you to selectively enable or disable certain plugin features to reduce UI clutter. You can control the visibility of:

- Preference pages
- Property pages
- Launch configurations
- Console factory

To manage these features:
1. Go to **Window → Preferences → General → Capabilities**
2. Find the "Cucumber" category
3. Check or uncheck the activities you want to enable or disable

## Setting Up Development Environment

### Prerequisites

- **Eclipse IDE**: Eclipse IDE for Eclipse Committers (2024-06 or later) - [Download](https://www.eclipse.org/downloads/packages/)
- **Java 21**: JDK 21 or higher - [Download](https://adoptium.net/)
- **Maven**: Maven 3.6+ (for command-line builds) - [Download](https://maven.apache.org/)

### Option 1: Using Oomph (Recommended)

[Oomph](https://wiki.eclipse.org/Eclipse_Oomph_Installer) is Eclipse's installation and project provisioning system that automates the setup process.

1. **Download and launch Eclipse Installer**
   - Download from [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer)
   - Start the Eclipse Installer

2. **Switch to Advanced Mode**
   - Click the menu icon (☰) in the top-right corner
   - Select "Advanced Mode"

3. **Add the Cucumber Eclipse Setup**
   - Click the "+" icon to add a new project
   - Select "Github Projects" as the catalog
   - Paste the setup file URL:
     ```
     https://raw.githubusercontent.com/cucumber/cucumber-eclipse/main/CucumberEclipse.setup
     ```
   - Or use a local file if you've already cloned the repository:
     ```
     file:/path/to/cucumber-eclipse/CucumberEclipse.setup
     ```

4. **Select Product and Project**
   - Select "Eclipse IDE for Eclipse Committers" as the product
   - Check "Cucumber Eclipse" in the project list
   - Click "Next"

5. **Configure Variables**
   - Set your desired installation and workspace locations
   - Configure GitHub credentials (if you want to contribute)
   - Click "Next" and then "Finish"

6. **Wait for Setup**
   - Oomph will now:
     - Install required Eclipse features (PDE, M2E)
     - Clone the Cucumber Eclipse repository
     - Set up the target platform
     - Import all plugin projects
     - Configure workspace preferences
   - This may take several minutes depending on your internet connection

7. **Start Development**
   - Once setup completes, Eclipse will open with all projects imported
   - The target platform will be automatically set
   - You're ready to start developing!

### Option 2: Manual Setup

If you prefer to set up the environment manually:

1. **Clone the repository**
   ```bash
   git clone https://github.com/cucumber/cucumber-eclipse.git
   cd cucumber-eclipse
   ```

2. **Import projects into Eclipse**
   - Open Eclipse IDE for Eclipse Committers
   - Go to `File` → `Import` → `General` → `Existing Projects into Workspace`
   - Select the cloned repository root directory
   - Make sure "Search for nested projects" is checked
   - Select all projects and click "Finish"

3. **Set the target platform**
   - Open `io.cucumber.eclipse.targetdefinition/cucumber.eclipse.targetdefinition.target`
   - Wait for the target platform to resolve (this may take a few minutes)
   - Click "Set as Active Target Platform" in the top-right corner of the editor

4. **Configure Java 21**
   - Go to `Window` → `Preferences` → `Java` → `Installed JREs`
   - Add JDK 21 if not already present
   - Ensure it's set as the default

5. **Build the workspace**
   - Go to `Project` → `Clean...`
   - Select "Clean all projects" and click "OK"
   - Wait for the automatic build to complete

## Build and install from source

To use the latest features, you can choose to build and install from source.

- Build the plugin using Maven (https://maven.apache.org/) <code>mvn clean install</code>
- Open Eclipse and navigate to `Help` -> `Install New Software...` -> `Add`
- Point to the update-site built in step 1 <code>file:path_to_repo/io.cucumber.eclipse.updatesite/target/repository</code>
- Proceed to install like any other plug-in

## How soon will my ticket be fixed?

The best way to have a bug fixed or feature request implemented is to
to fork the cucumber-eclipse repository and send a
[pull request](http://help.github.com/send-pull-requests/).
If the pull request is reasonable it has a good chance of
making it into the next release. If you build the release yourself, even more chance!

If you don't fix the bug yourself (or pay someone to do it for you), the bug might never get fixed. If it is a serious
bug, other people than you might care enough to provide a fix.

In other words, there is no guarantee that a bug or feature request gets fixed. Tickets that are more than 6 months old
are likely to be closed to keep the backlog manageable.
