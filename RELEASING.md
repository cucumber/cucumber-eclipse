# Releasing

## Check [![Build Status](https://travis-ci.org/cucumber/cucumber-eclipse.svg?branch=master)](https://travis-ci.org/cucumber/cucumber-eclipse) ##

Is the build passing?

```bash
git checkout master
mvn -DreleaseVersion=1.0.0 -Dtag=1.0.0 -DdevelopmentVersion=1.0.1-SNAPSHOT -DdryRun=true clean javadoc:javadoc release:clean release:prepare
```

## Setup Maven

The release will update automatically the GitHub repository with the new tag and update the master with the next version release.

Thus, we need to indicate your GitHub credential to Maven. Then, update your `~/.m2/settings.xml` to add your GitHub account.

```xml
<?xml version="1.0"?>
<settings>
  <servers>
    <server>
      <id>github.com</id>
      <username>GITHUB ACCOUNT ID</username>
      <password>GITHUB OAUTH TOKEN</password>
    </server>
  </servers>
</settings>
```

## Release

```bash
 mvn -DreleaseVersion=1.0.0 -Dtag=1.0.0 -DdevelopmentVersion=1.0.1-SNAPSHOT clean javadoc:javadoc release:clean release:prepare
```

This command will 

 * update the version in the Maven POMs and Eclipse plugins MANIFESTs
 * build the project
 * push the tag
 * update a second time the version for the next development version
 * update the Eclipse update sites for [snapshot](https://github.com/qvdk/cucumber-eclipse-updates-snapshot) or [release](https://github.com/qvdk/cucumber-eclipse-updates)

## Update the Cucumber Eclipse website

Share the release notes on the official Cucumber Eclipse website.

```bash
git clone -b gh-pages git@github.com:cucumber/cucumber-eclipse.git cucumber-eclipse-site
```

In the `_posts` directory, add a new .md file for your version, giving brief release notes.
