# Performance Tuning Guide

This guide describes how to improve the responsiveness of the Cucumber Eclipse plugin when working with large projects.
Large projects — those with many feature files, many step definitions, or large Java classpaths — can experience slow background jobs such as **"Compute Step Definitions"** and **"Searching Java Glue Code"**.
The sections below explain what drives that cost and what you can do about it today.

---

## Understanding What the Plugin Does

When you open a feature file or save Java glue code, the plugin runs two main activities:

1. **Glue scanning** — The plugin starts a Cucumber JVM runtime in dry-run mode using the project's classpath.
   It loads all step definitions, matches them against your feature file's steps, and stores the results.
   This is the expensive part: it involves creating a classloader, scanning the classpath for step definition classes, and loading them.

2. **Step matching** — For each feature file the plugin checks which steps are matched and which are missing.
   It creates or removes problem markers and refreshes content-assist suggestions.

Both activities run as Eclipse background jobs.
They are re-triggered whenever a glue file (Java class with Cucumber annotations) is saved or a feature file is changed.

---

## Option 1: Configure Glue Path Filters (Most Impactful)

By default the plugin scans **all packages** on the project's classpath for step definitions.
In a large project with many dependencies, this scanning touches a lot of classes needlessly.

Restricting the scan to only the packages that actually contain your step definitions is the single most effective performance improvement.

### Setting workspace-wide filters

1. Open **Window → Preferences → Cucumber → Java Backend**.
2. In the **Step Definition Filters** table, click **Add Filter** or **Add Package**.
3. Enter the package prefix of your step definitions, for example `com.example.steps`.
4. Make sure the filter is **checked** (active).
5. Click **Apply and Close**.

Only classes in matching packages will be scanned for `@Given`, `@When`, `@Then`, and other Cucumber annotations.
Everything else is ignored.

### Setting project-specific filters

If different projects in your workspace use different step-definition packages, use project-level overrides:

1. Right-click your project → **Properties → Cucumber Java Options**.
2. Check **Enable project specific settings**.
3. Add your project's step-definition package(s) to the filter table.
4. Click **Apply and Close**.

### Tips for effective filters

- Use the most specific package prefix that covers all your step definitions (e.g. `com.myapp.bdd.steps` instead of `com`).
- If your step definitions span multiple packages, add one filter entry per package.
- Wildcards are not supported — add each root package separately.
- Filters use prefix matching, so `com.example.steps` also matches `com.example.steps.order` and deeper sub-packages.

---

## Option 2: Disable the Automatic Project Builder

The optional **Cucumber Builder** validates every `.feature` file in your project automatically on each build.
In a project with hundreds of feature files this can be very slow, especially after a classpath change.

Consider disabling the builder and relying on the lighter on-demand validation that runs only for files currently open in the editor:

1. Right-click your project in the Project Explorer.
2. Choose **Project → Configure → Disable Cucumber Builder**.

On-demand validation starts automatically when you open a feature file and repeats whenever you edit it.
The builder is only needed if you want markers on **all** feature files at once (for example in a CI-like review workflow inside Eclipse).

---

## Option 3: Increase the Validation Delay (Debounce Timeout)

By default the plugin waits 500 ms after you stop typing before it re-validates the current feature file.
On a slow machine you may prefer a longer delay to reduce the number of validation runs while you type.

### Setting the timeout workspace-wide

1. Open **Window → Preferences → Cucumber → Editor**.
2. Adjust the **Validation Delay** field (value in milliseconds).
3. A value of `1000`–`2000` ms gives the editor time to settle before a validation run starts.

### Setting the timeout per project

1. Right-click your project → **Properties → Cucumber Editor Options**.
2. Check **Enable project specific settings**.
3. Set the desired **Validation Delay** for that project.

---

## Option 4: Organise Projects for Faster Scanning

If you have full control over your project structure, consider these layout choices:

- **Keep step definitions in a dedicated project or source folder** that contains no other classes.
  When the glue path filter points only to this project/folder, the scanner has far fewer classes to inspect.

- **Split large multi-module projects** so that each sub-module has its own small set of feature files and step definitions.
  Eclipse validates each project independently, so a change in one module does not trigger a full rescan of another.

- **Avoid placing feature files in build output folders** (e.g. `target/` or `bin/`).
  The plugin excludes derived resources from scanning, but having feature files outside source folders can create duplicates.

---

## Option 5: Reduce the Number of Tracked Feature Files

The plugin tracks feature files for background validation.
The more files are tracked, the more work is done when any glue code changes.

- Use the builder only for projects where you actively work on step definitions.
- Close projects you are not actively working on (**Project → Close Project**) — closed projects are not scanned.

---

## Diagnosing Performance Problems

If you are not sure which part is slow, enable the plugin's built-in performance tracing.

### Enable tracing in Eclipse

1. Start Eclipse with the `-debug` flag, or create a `.options` file in your Eclipse installation directory.
2. Add the following entries and set them to `true`:

   ```
   io.cucumber.eclipse.editor/perf=true
   io.cucumber.eclipse.editor/perf/steps=true
   ```

3. Restart Eclipse.
4. Open the **Error Log** view (**Window → Show View → Error Log**) or the **Console** to see timing output.

### What the trace shows

| Option | What is measured |
| -------| ---------------- |
| `perf` | Validation job start/end, document counts, revalidation trigger counts, builder stats |
| `perf/steps` | Per-phase timing inside glue validation: classloader creation, feature parsing, Cucumber runtime execution (glue load + step matching), JDT type resolution for content assist |

**Sample output with `perf/steps=true`** (what to look for):

```
CucumberRuntime created for 'my-project' with 342 classpath URL(s)
[my-project] Classloader created in 1843ms
[my-project] Parsed 12/12 feature(s) in 28ms
[my-project] Runtime (glue load + match) took 3210ms for 12 feature(s), glue filters: []
  login.feature: 8/47 steps matched, 0 missing
  checkout.feature: 12/47 steps matched, 0 missing
[my-project] Total glue validation: 5124ms (12 doc(s), 47 step def(s))
findStepDefinitions: resolving 47 step def(s) via JDT for checkout.feature
findStepDefinitions: resolved 47/47 in 612ms
```

**How to interpret the numbers:**

- A high **Classloader created** time (> 500ms) and a large classpath URL count (> 200) means classloader caching (planned improvement) will help significantly.
- A high **Runtime (glue load + match)** time with empty `glue filters: []` means adding glue path filters (Option 1) will directly reduce that time.
- A high **findStepDefinitions** time means content-assist JDT type resolution is slow; this will be addressed by caching (planned improvement).
- If **Parsed N/N feature(s)** shows many features being parsed each time, limiting the revalidation scope to open editors (planned improvement) will reduce that count.

Additional debug options (useful when investigating unexpected behaviour, not just performance):

| Option | What is traced |
| -------| -------------- |
| `debug/validation` | Validation lifecycle events (document open, change, close) |
| `debug/validation/glue` | Java glue code change detection events |
| `debug/steps` | Step definition discovery events |
| `debug/search` | JDT search operations |
| `debug/launching` | Launch configuration events |

### Reading the output

Look for lines like:

```
Verify Features: starting validation of 85 document(s)
revalidateDocuments(my-project): 2 editor doc(s), 83 background doc(s) scheduled
[my-project] Classloader created in 1843ms
[my-project] Runtime (glue load + match) took 3210ms for 85 feature(s), glue filters: []
[my-project] Total glue validation: 5124ms (85 doc(s), 47 step def(s))
Verify Features: finished in 5202ms (85/85 syntax-valid)
```

The **revalidateDocuments** line is the most diagnostic: it shows how many documents are queued each time a glue change is detected.
A high background doc count (like 83 in the example above) means many feature files are being revalidated unnecessarily — this is the core problem that incremental improvements address.

A high **Classloader created** time combined with a large classpath URL count is the next bottleneck.
A high **Runtime** time with `glue filters: []` means adding glue path filters (Option 1) will directly reduce that time.

---

## Summary Table

| Situation | Recommended action |
| --------- | ------------------ |
| Slow after saving a Java glue file | Option 1 (glue path filters) |
| Slow on project build / startup | Option 2 (disable builder) |
| Slow while typing in a feature file | Option 3 (increase validation delay) |
| Large monorepo with many modules | Option 4 (project structure) |
| Many unused projects open | Option 5 (close inactive projects) |
| Not sure what is slow | Enable performance tracing |

---

## Known Limitations

The following limitations are known and are being addressed in upcoming releases:

- When any Java glue file changes, **all feature files** in the project are re-validated, not only those affected by the change.
  Incremental glue updates (validating only the relevant subset of features) are planned.

- The Cucumber runtime classloader is currently re-created on every validation run.
  Classloader caching per project is planned, which will significantly reduce startup overhead per validation cycle.

- Step matching for content assist resolves Java types via JDT on every invocation.
  Caching these resolutions is planned.

Progress on these items is tracked in the project's [issue tracker](https://github.com/cucumber/cucumber-eclipse/issues).
