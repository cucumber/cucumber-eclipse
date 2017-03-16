---
layout: post
title: Reusing Step-Definitions From Class-Path Dependency
---
Version: 0.0.16.201703160952
New Reusing Step-Definitions From Class-Path Dependency :
---
### Description :
* Cucumber-Step-Definitions can be bundled and reused from any external Class-Path Dependencies(JAR/POM...etc) :
* JAR MUST be added into your project class-path (or) the dependency for the project can be added in maven-pom file.
* This feature can be enabled through **'Cucumber User Settings'** Preference Page : 
** User MUST have to configure the **ROOT Package Name** (ex. com.motive.bdd.smp/com.motive.bdd/com.motive...etc)  of external Dependencies(JAR/POM...etc) contains all Step-Definition files.
** All external Step-Definition list will be auto populated in feature file through 'Content Assistance' feature.

### How To Use This Feature :
* Build and Bundle your required project into JAR/Maven-Dependency having Step-Definition files
* Uninstall Older version of cucumber-eclipse plugin (Refer : Plugin Installation Process )
* Install New Version (0.0.16.201703160952) plugin from Eclipse-Update-Site of Motive Repository
* Restart your eclipse after installation.
* Add the bundled JAR file into your Current Project Class-Path (or) Add the dependency for the project in maven-pom file.
* MUST Close all the Feature files if already opened in Eclipse Editor.
* Open **'Cucumber User Settings'** Preference Page from Eclipse :
* Click on **'Window > Preference > Cucumber > User Settings' :** 
* MUST Add your ROOT package name of JAR/pom-dependency (ex. com.motive.bdd.smp/com.motive.bdd/com.motive...etc) into **'Add Root Package Name Of Your Class-Path Dependency(JAR/POM...etc)'** input field of **'Cucumber User Settings'** Preference page.
* Click on **'Apply > OK'**
* Open any Feature File in which step-definitions need to be reused/imported from external dependency(JAR/POM..etc)
* Use **'[Ctrl]+[Space]'** keys to activate 'Content Assistance' feature :
* All Step-definition proposals are populated based on the configured package name of external class-path dependencies(JAR/POM..etc)