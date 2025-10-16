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
