---
layout: post
title: Content Assistance for feature file
---
Version: 0.0.13.201603281006
Content Assistance for feature file
---
-'Content Assistance' for Eclipse editor can be activated by pressing [Ctrl]+[Space]' keys while writing any feature file.
- Supports proposals for list of Keywords.
- Supports proposals for Predefined-Steps/Search based Predefined-Steps by '[Predefined-Step]:[Step-Definition(java)File]' format.
- Some rules for Content Assistance are highlighted below :
a. Rules for Keyword Assistance :
---
- Populates all Keywords proposal by pressing [Ctrl]+[Space] keys.
- New line starts with((First word)) any blank space : populates all keywords.
- New line starts with(First word) any matched and unmatched keyword(Ex. Given/abcd123) : populates only Step-keywords(Given,When,Then,And,But)
b. Rules for step Assistance :
---
- Populates all/search based predefined steps proposal by pressing '[Ctrl]+[Space]' keys.
- New line MUST be starts with a valid Step-Keyword.
- If Next word is blank : Populates all predefined steps
- If Next word starts with any matched step : Populates only matched predefined steps
- If Next word starts with any unmatched step : Populates 'No Proposals'