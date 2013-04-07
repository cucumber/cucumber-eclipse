# Cucumber-Eclipse

Eclipse plugin for Cucumber. This repo will host what comes out of the collabortive effort that results from
[this discussion](https://groups.google.com/d/topic/cukes/UzywqbOmqBc/discussion).

Installation and further information
====================================

Please head over to the [plugin website](http://cucumber.github.com/cucumber-eclipse) for more information.

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



