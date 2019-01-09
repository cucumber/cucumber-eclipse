Working on the plugin
=====================

You should be able to work on the plugin by cloning this repository:

    git clone git@github.com:cucumber/cucumber-eclipse.git

You can then import the 4 plugin projects into Eclipse.

We have arbitrarily decided to support Eclipse 3.5+ - hopefully this will cover most development kits that don't stick to the bleeding edge of Eclipse versions, however if there is an overriding reason to use a more recent version of the eclipse APIs, this is not set in stone. When adding dependencies to the plugins, make sure to add them as version [3.5,0).

Modules list
============

Plugins:

* cucumber.eclipse.editor - an editor for .feature files - will use step definitions from integration implementions if they exist
* cucumber.eclipse.runner - runs a feature file wrapped up in a JUnit test. Initially will depend on the steps integration implementation.
* cucumber.eclipse.steps.integration - an extension point for plugins that supply a means to deduce a list of all steps defined in a project
* cucumber.eclipse.steps.jdt - an implementation of the integration extension point that uses Eclipse JDT to find steps.

Feature:

* cucumber.eclipse.feature - an Eclipse feature set that includes all of the above plugins.
