# 🌱 Rently Backend — SSE Version

## 📌 Overview

This repository contains the **backend of the Rently project**, originally developed by students of the Software Engineering course.
We forked the original project (full credit to the authors: [https://github.com/rently-unisa/back-end](https://github.com/rently-unisa/back-end)) and extended it as part of the **Sustainable Software Engineering (SSE)** course.

The objective of this work is to **evaluate and improve the environmental, technical, and performance sustainability** of the backend using the tools introduced during the course.

The repository includes three branches used during the analysis:

```
├─ baseline/            # version before sustainability evaluations
├─ sse-improvements/    # version where sustainability enhancements were applied
└─ main                 # final integrated backend
```

Experiment logs, tool outputs, and structured reports are stored under:

```
sse-reports/
   ├─ baseline/
   └─ sse-improvements/
```

Each directory contains:

* Raw tool results
* CSV logs
* Screenshots
* Analysis notes

---

## 🌍 Sustainability Tools

As part of the SSE course activities, the backend was evaluated using the tools presented during the laboratory sessions.
Each tool contributes to a different dimension of software sustainability.

---

### 🖼️ **GreenIT-Analysis (Static Assets Optimization)**

Although GreenIT-Analysis is typically used to evaluate web pages, in this project it was also applied to the backend to analyze and optimize **static assets served by the API**, such as:

* Images
* Static resources under `src/main/resources/static`
* Media files exposed by the application

GreenIT-Analysis helped identify:

* Excessively large or uncompressed images
* Unnecessary resource weight
* Potential improvements in page performance for clients consuming backend-served assets

---

### ✅ **Creedengo (via SonarQube plugin)**

Creedengo is a static analysis tool designed to detect **energy-inefficient Java code patterns**, including:

* Variable mutability issues
* Inefficient loops
* Unnecessary object allocations
* Suboptimal data structures

Creedengo is executed through a local SonarQube instance and assists in identifying areas of potential energy improvement within the codebase.

---

### ⚡ **EnergiBridge**

EnergiBridge is a cross-platform **energy measurement tool** used to analyze runtime resource consumption.
It enables the evaluation of:

* CPU energy usage
* Power consumption fluctuations
* Resource utilization under controlled workloads
* Memory usage patterns

EnergiBridge is used to measure backend execution during reproducible execution scenarios.

---

### 🧪 **JMeter**

JMeter is used to perform **performance and workload evaluation** of the backend, following the methodologies introduced in the SSE course.

It enables the systematic assessment of:

* Response times
* Throughput
* Stability under load
* Resource usage under stress

JMeter supports several categories of performance testing:

* **Performance Testing** — evaluates execution time, throughput, and resource usage to detect performance regressions.
* **Load Testing** — applies gradually increasing workload to assess stability under normal operating conditions.
* **Stress Testing** — overloads the system to evaluate behavior under extreme pressure and recovery characteristics.
* **Spike Testing** — introduces sudden bursts of traffic to examine how the system reacts to rapid load fluctuations.
* **Soak Testing** — maintains sustained load over extended periods to detect long-term degradation or resource leaks.

JMeter is executed locally to collect performance indicators that support backend sustainability evaluation.

---

### 🚀 **JMH**

JMH (Java Microbenchmark Harness) is used to analyze **micro-level performance characteristics** of isolated backend components.
It is appropriate for benchmarking:

* Utility functions
* Business logic routines
* Computation-heavy operations
---

## 🔐 **FOSSA (License Compliance & Open-Source Governance)**

FOSSA is an automated tool integrated into the project’s CI/CD pipeline to ensure **license compliance**, detect dependency conflicts, and verify compatibility with the project’s chosen license (MIT).
It performs static analysis of all dependencies (direct and transitive) and generates:

* A complete **dependency inventory**
* License compatibility checks
* Identification of potential **legally risky libraries**

In this project, FOSSA was used to:

* Validate that all dependencies comply with MIT licensing
* Detect and handle flagged or ambiguous licenses early in CI/CD
* Ensure sustainable long-term maintainability by avoiding compliance debt

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B59104%2Fgithub.com%2Fredyz13%2FSSE-backend.svg?type=shield&issueType=license)](https://app.fossa.com/projects/custom%2B59104%2Fgithub.com%2Fredyz13%2FSSE-backend?ref=badge_shield&issueType=license)
---

## 🧭 **GUIDO (Community Smell Detection Tool)**

GUIDO is an academic tool developed at the University of Salerno to identify **community smells**—undesirable patterns of collaboration or communication that may lead to social or organizational debt.

GUIDO operates as an interactive chatbot that:

1. Collects information on the team structure and collaboration habits
2. Computes metrics such as **Dispersion Value**, communication redundancy, and cross-team connectivity
3. Detects communication patterns.
4. Suggests refactoring strategies for healthier, more sustainable teamwork.

---

## 🐳 Running the Backend with Docker

### 1️⃣ Create a `.env` file

Create a `.env` file in the project root with the following variables:

```
MYSQL_ROOT_PASSWORD=your_root_pw
MYSQL_DATABASE=rently
MYSQL_USER=your_user
MYSQL_PASSWORD=your_pw

SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/rently?useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=${MYSQL_USER}
SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}

SPRING_JPA_HIBERNATE_DDL_AUTO=update

MAIL_HOST=your_mail_host
MAIL_PORT=587
MAIL_USERNAME=your_user
MAIL_PASSWORD=your_pw

SECURITY_SECRET=your_jwt_secret
```

Note: `db` inside `SPRING_DATASOURCE_URL` refers to the Docker Compose service name, you can use localhost for local execution.

---

### 2️⃣ Run the Backend

```bash
docker compose up --build
```

This command starts:

* `db` → MySQL 8.0
* `backend` → Spring Boot container

The backend will be available at:

```
http://localhost:4000
```

---

## 👥 Credits

Original backend authors: [https://github.com/rently-unisa/back-end](https://github.com/rently-unisa/back-end)
