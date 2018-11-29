---
layout: post
title: New release 0.0.23
---
Version: 0.0.23.201811220126 
New release 0.0.23
---

### New features

 * add preference to configure indentation style #284

<img src="/cucumber-eclipse/assets/indentation_preference.png" alt="Indentation preference" width="680px" />

 * go to step definition from a feature file with <kbd>CTRL</kbd>+<kbd>click</kbd> #290

<img src="/cucumber-eclipse/assets/goto_ctrl_click.gif" alt="Go to step definition" width="680px" />

### Improvements

 * faster gherkin editor opening time #281  
 * resolution of step definitions from parent projects #289 
 * support of cucumber expressions #285

<img src="/cucumber-eclipse/assets/cucumber_expression.png" alt="Support cucumber expression" width="680px" />

### Bug fixes

 * Fix gherkin editor crash on wrong regexp in a step definition #286
 * Fix gherkin editor crash when using cucumber expressions #267 #278
 * Fix string literal syntax highlighting #280

### Known limitations

 * Custom parameter types in cucumber expressions are not detected. Thus, the expression `I have a {word} cat` is supported, but `I have a {color} cat` will not. #291 

<img src="/cucumber-eclipse/assets/cucumber_expression_limitation.png" alt="Support cucumber expression limitation" width="680px" />
