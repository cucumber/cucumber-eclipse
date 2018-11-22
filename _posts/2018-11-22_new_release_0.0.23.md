---
layout: post
title: New release 0.0.23
---
Version: 0.0.23.201811220126 
New release 0.0.23
---

### New features

 * add preference to configure indentation style #284

![Indentation preference](/images/_posts/2018-11-22/indentation_preference.png)

 * go to step definition from a feature file with <kbd>CTRL</kbd>+<kbd>click</kbd> #290

![Go to step definition](/images/_posts/2018-11-22/goto_ctrl_click.gif)

### Improvements

 * faster gherkin editor opening time #281  
 * resolution of step definitions from parent projects #289 
 * support of cucumber expressions #285

![Support cucumber expression](/images/_posts/2018-11-22/cucumber_expression.png)

### Bug fixes

 * Fix gherkin editor crash on wrong regexp in a step definition #286
 * Fix gherkin editor crash when using cucumber expressions #267 #278
 * Fix string literal syntax highlighting #280

### Known limitations

 * Custom parameter types in cucumber expressions are not detected. Thus, the expression `I have a {word} cat` is supported, but `I have a {color} cat` will not. #291 

![Support cucumber expression limitation](/images/_posts/2018-11-22/cucumber_expression_limitation.png)
