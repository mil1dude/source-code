name: Bug Report
description: Report a reproducible bug or crash in the Eaglercraft 1.21.4 workspace.
title: "[Bug] "
labels: [bug]
assignees: mil1dude

body:
  - type: textarea
    attributes:
      label: What happened?
      description: Describe what went wrong. Include logs, errors, screenshots if possible.
    validations:
      required: true

  - type: textarea
    attributes:
      label: Steps to reproduce
      description: Provide a step-by-step guide to reproduce the issue.
    validations:
      required: true

  - type: input
    attributes:
      label: Browser and Platform
      description: e.g., Chrome 124 / Firefox 127 / OS / mobile / desktop etc.
    validations:
      required: true

  - type: textarea
    attributes:
      label: Additional information
      description: Any other context, log files, or code snippets.
    validations:
      required: false
