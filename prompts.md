# AI Usage and Prompts Log

This document details the interaction with the AI assistant during the design and development of the IssueFlow backend.

## AI Model Used
* **Model**: 
- Gemini 3.1 Pro for simple tasks
- Claude 3.5 Sonnet / Opus for more complicated tasks like Architecture decisions and API Contract extraction
- (via Antigravity IDE)

## AI Usage Summary
The AI was used to:
1. Parse the assignment requirement documents and extract the API contract.
2. Outline the layered package structure (`com.att.tdp.issueflow`).
3. Formulate the implementation plan, test plan, and identify project ambiguities.
4. Generate the boilerplate entities, repositories, services, controllers, and tests.

## Key Prompts
* **Prompt 1 (Analysis)**: *"read the project (do not change anything) and tell me what you understand from this structure that i need to implement"*
* **Prompt 2 (API Contract)**: *"Read README.md carefully and extract the full API contract."*
* **Prompt 3 (Gap Analysis)**: *"Compare the current project and README API contract against the assignment requirements..."*
* **Prompt 4 (Structure)**: *"Propose a clean Spring Boot package structure for this assignment."*
* **Prompt 5 (Plan)**: *"Create an implementation plan for this project in phases..."*
* **Prompt 6 (Test Plan)**: *"Create a test plan for the IssueFlow assignment..."*
* **Prompt 7 (Ambiguities)**: *"Review the assignment requirements and identify ambiguous or risky areas."*
* **Prompt 8 (Phase 6 — Tests)**: *"Implement Phase 6 — Tests: create test application.yaml, write auth, ticket, auto-assignment, auto-escalation, and attachment validation tests."*
* **Prompt 9 (Phase 7 — Documentation)**: *"Implement Phase 7 — Documentation: finalize run.md with exact commands, prompts.md with all prompts used, and seed data.sql with example users."*
