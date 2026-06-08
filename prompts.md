# AI Usage and Prompts Log

This document details the interaction with the AI assistant during the design and development of the IssueFlow backend.

## AI Model Used
* **Model**: 
- Gemini 3.1 Pro for simple tasks and questions 
- Claude 3.5 Sonnet / Opus for more complicated tasks like Architecture decisions and API Contract extraction
- (via Antigravity IDE)

## AI Usage Summary

1. AI tools were used as development assistants for requirement analysis, API contract extraction, implementation planning, test planning, and documentation review.
2. All AI-generated suggestions were reviewed, adapted, and validated through compilation, manual testing, and automated tests. I am fully accountable for that code.

## Key Prompts
* **Prompt 1 (Analysis)**: *"go over the current project skeleton (do not change anything) and tell me what you understand from this structure that i need to implement"*
* **Prompt 2 (API Contract)**: *"Read README.md carefully and extract the full API contract."*
* **Prompt 3 (Gap Analysis)**: *"Compare the current project and README API contract against the assignment requirements..."*
* **Prompt 4 (Design & Architecture)**: *"Propose a clean Spring Boot package structure for this assignment. Discuss the architecture decisions, specifically monolithic vs. microservices, and outline the system's scalability and capacity limits."*
* **Prompt 5 (Documentation Generation)**: *"Generate the project documentation files inside the 'docs' folder, including the API contract checklist, implementation decisions, rules, and system input restrictions."*
* **Prompt 6 (Plan)**: *"Create an implementation plan for this project in phases..."*
* **Prompt 7 (Test Plan & Ambiguities)**: *"Review the assignment requirements, identify ambiguous or risky areas, and create a comprehensive test plan."*

* **Prompt 8 - 14**: Implementing the project in steps according to the plan (rules.md)

* **Prompt 15+ (Security & Stress Testing)**: *"Conduct additional testing for the project, analyzing and hardening the system against SQL injection, and performing stress testing scenarios."*
