# Cucumber-Eclipse 2.0

[![Build Status](https://github.com/cucumber/cucumber-eclipse/actions/workflows/maven.yml/badge.svg)](https://github.com/cucumber/cucumber-eclipse/actions/workflows/maven.yml)

An Eclipse plugin for [Cucumber Version 6+](https://cucumber.io/). For the previous version 
of this plugin take a look at the [1.x](https://github.com/cucumber/cucumber-eclipse/tree/1.x) branch.

CAUTION: **This is currently work-inprogress!**

## Highlighted Features :

-
-
-

## Screenshots and Features of the plugin
-


## Eclipse-Marketplace Details
-

## Download the plugin
 - Releases and snapshots versions can be [downloaded](https://github.com/cucumber/cucumber-eclipse/releases) as zip format.
 - Please refer our [Plugin-Download/Installation-Wiki](https://github.com/cucumber/cucumber-eclipse/wiki/Download-and-Offline-Installation-Of-The-Plugin-From-Zip) for detail information

## Follow the latest snapshot

For users who wants to keep up-to-date with the latest development version, there is a dedicated [eclipse update site for the cucumber eclipse plugin](https://cucumber.github.io/cucumber-eclipse/update-site/main/). 

With this one, you will be notified on each new snapshot.

CAUTION: **The latest snapshot can be unstable. This is a preview version.**

You are welcome to [report any issue](https://github.com/cucumber/cucumber-eclipse/issues).

## Installation and further information

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
