# Cucumber Eclipse Activity Groups

This document describes the Eclipse Activity Groups defined for the Cucumber Eclipse plugin, which allow users to selectively enable or disable certain aspects of the plugin.

## Overview

Eclipse Activities provide a way to hide or show groups of related functionality. This is useful when you want to reduce UI clutter or disable features you don't use.

## Available Activity Groups

The Cucumber Eclipse plugin defines the following activity groups under the "Cucumber" category:

### 1. Cucumber Preferences
Controls the visibility of Cucumber preference pages:
- Main Cucumber preferences page
- Java Backend preferences page

### 2. Cucumber Properties
Controls the visibility of Cucumber property pages:
- Main Cucumber properties page
- Java Backend properties page

### 3. Cucumber Launching
Controls the visibility of Cucumber launch configurations:
- Cucumber Feature launch shortcuts
- Launch configuration types

### 4. Cucumber Console
Controls the visibility of:
- Cucumber Console factory

## How to Enable/Disable Activities

To manage these activities in Eclipse:

1. Go to **Window → Preferences** (or **Eclipse → Preferences** on macOS)
2. Navigate to **General → Capabilities**
3. Find the "Cucumber" category
4. Check or uncheck the activities you want to enable or disable
5. Click **Apply and Close**
6. Restart Eclipse for the changes to take full effect

## Default State

By default, all activities are enabled, meaning all Cucumber features are visible. You can disable activities to hide features you don't need.

## Pattern Matching

The activities use pattern matching to control UI elements. Each activity is bound to specific contribution IDs using regular expressions. This allows fine-grained control over which UI elements are shown or hidden.

## Technical Details

The activities are defined in the `plugin.xml` file using the `org.eclipse.ui.activities` extension point. Each activity has:
- An ID (e.g., `cucumber.eclipse.preferences`)
- A name (displayed in the UI)
- A description
- Pattern bindings that specify which contributions it controls

For more information about Eclipse Activities, see:
https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.isv/guide/workbench_advext_activities.htm
