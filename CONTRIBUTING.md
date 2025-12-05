# Contributing to CSPDog

Thank you for your interest in contributing! 🙇

CSPDog is an open-core project dedicated to making Content Security Policy (CSP) adoption in Java applications easier, safer, and more automatic.

This document explains how to contribute code, documentation, examples, and ideas.

---

# 📌 Types of Contributions Welcome

We welcome contributions in many forms:

## ✔ Code contributions
* Rewriter improvements, specially dealing with partial JSF/PF/RF responses
* CSP Level 3 (the hardest and upcoming) compliance improvements
* Nonce/hash logic enhancements
* Additional servlet container integrations
* Spring Boot or other framework adapters
* Performance optimizations
* Tests (unit, integration, fuzzing)

## ✔ Documentation
* Tutorials and how-tos
* Example configurations for popular frameworks
* Troubleshooting guides

## ✔ Bug reports
If something breaks, especially for:
* HTML rewriting edge cases
* Directive generation
* Invalid or missing CSP headers
* Unexpected interference with templating frameworks

## ✔ Feature requests
CSP evolves quickly; new directives or enforcement patterns are welcome.

## ✔ Security research
Safe contributions related to:
* HTML parsing
* Browser quirks
* CSP Level 2/3 compliance
* Bypass reports

# 🛠 Project Structure
```
/cspdog-core         → AGPL open-core library
/cspdog-enterprise   → Not publicly accessible; commercial features
/docs                → Documentation site
/examples            → Servlet + Spring Boot examples
/.github             → CI, issue templates
```
Only the open-core directory is part of this repository.

# 🐞 Reporting Issues
Before filing an issue:
1. Search existing issues — your problem may already be tracked.
2. Provide a minimal reproducible example.
3. If relevant, include:
   4. CSP headers emitted
   5. HTML snippets before/after rewriting
   6. Java version
   7. Servlet container version
   8. CSPDog version 
   9. Logs (masking sensitive data)

Use the **Bug Report** template provided in GitHub.

# 🧪 Running Tests
CSPDog uses Maven + JUnit + HTML fixture tests.

`mvn clean verify`

To run only parser/rewriter tests:

`mvn -pl cspdog-core -Dtest=Rewriter* test`

# 🔧 Build Requirements
* Java 17+
* Maven 3.9+
* Git

# 🔀 Branching Model

We follow a simplified Git workflow:
* master — stable, releasable code
* develop — active development
* feature/* — contribute new features
* fix/* — bug fixes
* docs/* — documentation-only updates

Please target PRs at develop, unless the patch is trivial.

# ✔ Pull Request Guidelines

A good PR:
- Links to an issue (or explains what it fixes).
- Is small and focused.
- Includes tests:
  - Unit tests for isolated logic
  - Snapshot/fixture tests for HTML rewriting
- Does not break backward compatibility unless explicitly discussed.
- Does not introduce commercial-only capabilities into the open-core.

Before submitting:

`mvn -q -DskipTests=false verify`

# 📝 Code Style
## Java
- Use standard Java 17 features
- Avoid dependencies unless absolutely necessary
- Keep HTML parsing deterministic
- Fail safe: rewriting must not corrupt HTML

# Documentation
- Markdown
- Diagrams using Mermaid
- Keep examples runnable

# 🔒 Security Policy
If you discover a security vulnerability:

**Do NOT open a public issue.**

Instead, please email: 📧 [security@cspdog.com](security@cspdog.com)

We respond within **48 hours** and follow a standard coordinated disclosure process.

# 🧩 Feature Flags & Open-Core Boundaries

To maintain clear separation:
- All rewriting logic, HTML parsing, nonce/hash computation, and basic config must remain open.
- Enterprise features (e.g., dashboards, compliance templates, advanced parsers, SLAs, integrations) are not part of the public repo.
If you're unsure whether a feature belongs to open-core, ask in GitHub Discussions first.

# 📄 License

By contributing, you agree that your contributions will be licensed under:

**AGPLv3 for the open-core repository**

CSPDog operates under a dual-license model; contributions may be referenced but are **not merged** into commercial code.

# 🤝 Our Commitment
We want CSPDog to be:
- Easy to contribute to
- Technically deep
- A welcoming community

Contributors of all experience levels are welcome.

Thank you for helping improve CSP adoption and make the web safer for everyone! ❤️ 🔒