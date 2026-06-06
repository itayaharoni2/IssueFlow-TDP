# AI Usage and Prompts Log

This document details the interaction with the AI assistant during the design and development of the IssueFlow backend.

## AI Model Used
* **Model**: Gemini 3.5 Flash / Claude 3.5 Sonnet (via Antigravity IDE)

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

## Accountability Review Note
As the developer, I have reviewed the generated code and verified that:
* It conforms to all business rules (forward-only lifecycle, blocker checks, etc.).
* Input validations are properly handled.
* Core integration tests pass successfully.
* I understand the implementation details of the authentication filters, optimistic locking, workload assignment, and file upload validators.
