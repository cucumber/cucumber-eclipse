# Testing Eclipse Activity Groups

This document describes how to test the Eclipse Activity Groups feature in the Cucumber Eclipse plugin.

## Prerequisites

- Eclipse IDE with the Cucumber Eclipse plugin installed (built from source with the activity support changes)
- A workspace with at least one Java project

## Test Plan

### Test 1: Verify Activities Appear in Capabilities Preferences

1. Open Eclipse
2. Navigate to **Window → Preferences → General → Capabilities**
3. Look for the "Cucumber" category in the list
4. Verify the following activities are listed under the Cucumber category:
   - Cucumber Preferences
   - Cucumber Properties
   - Cucumber Launching
   - Cucumber Console
5. All activities should be checked (enabled) by default

**Expected Result**: All four Cucumber activities should be visible and enabled by default.

### Test 2: Disable Cucumber Preferences Activity

1. In **Window → Preferences → General → Capabilities**
2. Uncheck the "Cucumber Preferences" activity
3. Click **Apply and Close**
4. Restart Eclipse
5. Navigate to **Window → Preferences**
6. Look for the "Cucumber" preference page

**Expected Result**: The Cucumber preference pages should not be visible in the preferences list.

### Test 3: Re-enable Cucumber Preferences Activity

1. In **Window → Preferences → General → Capabilities**
2. Check the "Cucumber Preferences" activity
3. Click **Apply and Close**
4. Restart Eclipse
5. Navigate to **Window → Preferences**

**Expected Result**: The Cucumber preference pages should now be visible again.

### Test 4: Disable Cucumber Properties Activity

1. In **Window → Preferences → General → Capabilities**
2. Uncheck the "Cucumber Properties" activity
3. Click **Apply and Close**
4. Restart Eclipse
5. Right-click on a project in the Project Explorer
6. Select **Properties**
7. Look for the "Cucumber" property page

**Expected Result**: The Cucumber property pages should not be visible in the project properties.

### Test 5: Disable Cucumber Launching Activity

1. In **Window → Preferences → General → Capabilities**
2. Uncheck the "Cucumber Launching" activity
3. Click **Apply and Close**
4. Restart Eclipse
5. Right-click on a `.feature` file
6. Select **Run As**
7. Look for "Cucumber Feature" option

**Expected Result**: The Cucumber launch shortcuts should not be visible in the Run As menu.

### Test 6: Disable Cucumber Console Activity

1. In **Window → Preferences → General → Capabilities**
2. Uncheck the "Cucumber Console" activity
3. Click **Apply and Close**
4. Restart Eclipse
5. Navigate to **Window → Show View → Other...**
6. Look for the Cucumber Console in the Console category

**Expected Result**: The Cucumber Console should not be available in the Show View list.

### Test 7: Disable All Activities

1. In **Window → Preferences → General → Capabilities**
2. Uncheck all Cucumber activities
3. Click **Apply and Close**
4. Restart Eclipse
5. Verify that none of the Cucumber UI elements are visible

**Expected Result**: All Cucumber-specific UI elements (preferences, properties, launch shortcuts, console) should be hidden.

### Test 8: Re-enable All Activities

1. In **Window → Preferences → General → Capabilities**
2. Check all Cucumber activities
3. Click **Apply and Close**
4. Restart Eclipse
5. Verify that all Cucumber UI elements are visible again

**Expected Result**: All Cucumber functionality should be restored.

## Manual Verification

After testing, manually verify the following:

1. **XML Validation**: The plugin.xml should be valid and not cause any errors during plugin loading
2. **Pattern Matching**: The activity patterns should correctly match the contribution IDs
3. **User Experience**: Disabling activities should provide a cleaner UI for users who don't use certain features
4. **Documentation**: The ACTIVITIES.md and README.md files should be accurate and helpful

## Automated Validation

The following automated validations have been performed:

- ✅ XML well-formedness check
- ✅ Activity structure validation (categories, activities, bindings, patterns)
- ✅ Pattern regex matching against actual IDs

## Known Limitations

- Activities require an Eclipse restart to take full effect
- Some UI elements may still be visible in unexpected places (this is normal Eclipse behavior)
- Activity state is stored in the Eclipse workspace, so it persists across sessions

## Troubleshooting

If activities don't work as expected:

1. Check the Eclipse Error Log (**Window → Show View → Error Log**)
2. Verify the plugin.xml syntax is correct
3. Ensure the activity patterns match the actual contribution IDs
4. Try restarting Eclipse with the `-clean` flag to clear cached plugin data
5. Check that the org.eclipse.ui.activities extension point is properly registered
