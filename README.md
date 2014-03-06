# Cucumber-Eclipse

Eclipse plugin for [Cucumber](http://cukes.info).

Installation and further information
====================================

Please head over to the [plugin website](http://cucumber.github.com/cucumber-eclipse) for more information.

After you install the Cucumber-Eclipse plugin, you can use it to run Cucumber-JVM. To do this, you will need to install all the libraries you want to use for Cucumber-JVM into your Eclipse project's build-path libraries. The likely candidates and their locations are in the download target at the [java-helloworld](https://github.com/cucumber/cucumber-jvm/blob/master/examples/java-helloworld/build.xml) example at GitHub.

Create a new feature file by selecting New => File from the menu and naming it with a ".feature" suffix to bring up the Feature Editor. After typing in the Gherkin code for a test, select Run => Run to invoke Cucumber-JVM on that feature. This will also create a run configuration, which you can modify and rename by selecting Run => Run Configurarations.... Tags are not available in Cucumber-Eclipse, but you can organize your features into directories and select the Feature Path that you want the run configuration to use. You can execute run configurations from the Run => Run History menu.

Another alternative is to use Cucumber-Eclipse for editing feature files and getting the generated step-definition stubs, but then running a Junit file with a @RunWith(cucumber.class) annotation similar to the java-helloworld [RunCukesTest.java](https://github.com/cucumber/cucumber-jvm/blob/master/examples/java-helloworld/src/test/java/cucumber/examples/java/helloworld/RunCukesTest.java). The @CucumberOptions most useful are

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

The full option list can be found at [CucumberOptions](https://github.com/cucumber/cucumber-jvm/blob/master/core/src/main/java/cucumber/api/CucumberOptions.java)

Modules list
============

Plugins:

* cucumber.eclipse.editor - an editor for .feature files - will use step definitions from integration implementions if they exist
* cucumber.eclipse.runner - runs a feature file wrapped up in a JUnit test. Initially will depend on the steps integration implementation.
* cucumber.eclipse.steps.integration - an extension point for plugins that supply a means to deduce a list of all steps defined in a project
* cucumber.eclipse.steps.jdt - an implementation of the integration extension point that uses Eclipse JDT to find steps.

Feature:

* cucumber.eclipse.feature - an Eclipse feature set that includes all of the above plugins.

Working on the plugin
=====================

You should be able to work on the plugin by cloning this repository:

<pre>git clone git@github.com:cucumber/cucumber-eclipse.git</pre>

You can then import the 4 plugin projects into Eclipse.

We have arbitrarily decided to support Eclipse 3.5+ - hopefully this will cover most development kits that don't stick to the bleeding edge of Eclipse versions, however if there is an overriding reason to use a more recent version of the eclipse APIs, this is not set in stone. When adding dependencies to the plugins, make sure to add them as version [3.5,0).

Releasing
=========

There is an Eclipse Update Site on the gh-pages branch. If you're going to do a release you'll need to clone that branch too:

<pre>git clone -b gh-pages git@github.com:cucumber/cucumber-eclipse.git cucumber-eclipse-site</pre>

You can then import the update-site subdirectory as an Eclipse project, and you'll also need to import the cucumber.eclipse.feature directory of the main code that you're using.

The build procedure is then:
* Update version numbers by running `mvn tycho-versions:set-version -DnewVersion=0.0.4-SNAPSHOT`
* Build the Eclipse Plugin, Feature and P2 repository with `mvn clean install`
* Copy the contents of `cucumber.eclipse.p2updatesite\target\repository` to the `update-site` folder in the cucumber-eclipse-site
* In the _posts directory, add a new .md file for your version, giving brief release notes.
* Git-add all changes, and commit using a github issue that covers the release, commit and push to relevant branches.


Screenshots and Features of the plugin
======================================
Please consult the [wiki](https://github.com/cucumber/cucumber-eclipse/wiki) for a full list for currently available features and screenshots.
eg [Syntax Highlighting](https://github.com/cucumber/cucumber-eclipse/wiki/I18n-Syntax-highlighting)



