# Behave project - basic structure

This repository contains a minimal structure for a Behave (Gherkin) project with a working example.

Structure created:

- features/
  - example.feature         (Gherkin feature example)
  - environment.py          (Behave hooks)
  - steps/
    - example_steps.py              (step definitions)
- behave.ini                (Behave configuration)
- requirements.txt          (dependencies: behave)
- .gitignore

Quick start:

1) Install dependencies (recommended in a virtual environment):

   python3 -m pip install -r requirements.txt

2) Run all the features from the featueres folder or project root:

    behave

2) Run a specific feature from the featueres folder or project root:

    behave example.feature

3) Run a specific scenario

    behave example.feature -n "Increment a number"
