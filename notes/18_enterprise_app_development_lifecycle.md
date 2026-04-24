# 18. Enterprise Android App — Complete Development Lifecycle

> End-to-end journey from idea to production for Banking / Healthcare / Finance apps.
> This guide covers what happens at every stage, who is involved, and why decisions are made.

---

## TABLE OF CONTENTS

```
Phase 1:  Discovery & Planning
Phase 2:  UI/UX Design
Phase 3:  Architecture & System Design
Phase 4:  Backend Development
Phase 5:  Database Design
Phase 6:  Android App Development
Phase 7:  API Integration & Data Flow
Phase 8:  Security & Compliance
Phase 9:  Testing Strategy
Phase 10: CI/CD Pipeline
Phase 11: Deployment & Release
Phase 12: Post-Launch Operations
```

---

## PHASE 1: DISCOVERY & PLANNING

> **Goal:** Understand WHAT to build, WHO it's for, and WHAT constraints exist.
>
> Before a single line of code is written, enterprise apps spend significant time
> understanding the problem. In regulated industries like banking or healthcare,
> skipping this phase can mean building something that is legally non-compliant,
> resulting in fines or rejected app submissions.

### 1.1 The Big Picture — SDLC (Software Development Lifecycle)

Every enterprise app follows an iterative lifecycle. Unlike personal projects where you
might just start coding, enterprise apps must go through formal stages because:
- Millions of dollars and user trust are at stake
- Regulatory bodies (RBI, FDA, SEC) audit the process
- Multiple teams (30+ people) need coordination

```
╔═════════════════════════════════════════════════════════════════════════╗
║                    ENTERPRISE APP LIFECYCLE (SDLC)                     ║
║                                                                         ║
║         ┌───────────────┐                                               ║
║         │  1. DISCOVER   │  ← What's the problem? Who are the users?   ║
║         │  & PLAN        │    What regulations apply?                   ║
║         └───────┬───────┘                                               ║
║                 ▼                                                       ║
║         ┌───────────────┐                                               ║
║         │  2. DESIGN     │  ← UI/UX mockups, system architecture,      ║
║         │  & ARCHITECT   │    API contracts, database schema            ║
║         └───────┬───────┘                                               ║
║                 ▼                                                       ║
║         ┌───────────────┐                                               ║
║         │  3. BUILD      │  ← Backend APIs, Android app, databases,    ║
║         │  (Development) │    integrations with third-party services    ║
║         └───────┬───────┘                                               ║
║                 ▼                                                       ║
║         ┌───────────────┐                                               ║
║         │  4. TEST       │  ← Unit, integration, E2E, security,       ║
║         │  & QA          │    performance, compliance testing          ║
║         └───────┬───────┘                                               ║
║                 ▼                                                       ║
║         ┌───────────────┐                                               ║
║         │  5. DEPLOY     │  ← CI/CD pipeline, staged rollout,         ║
║         │  & RELEASE     │    Play Store, backend to cloud             ║
║         └───────┬───────┘                                               ║
║                 ▼                                                       ║
║         ┌───────────────┐                                               ║
║         │  6. MONITOR    │  ← Crash rates, performance, user          ║
║         │  & OPERATE     │    feedback, incident response              ║
║         └───────┬───────┘                                               ║
║                 │                                                       ║
║                 └──────────────→ Back to Step 1 (next sprint/release)  ║
║                                                                         ║
╚═════════════════════════════════════════════════════════════════════════╝
```

### 1.2 Stakeholder Map

In enterprise, you don't just build for users — you build for a complex ecosystem
of people who all have different needs:

```
                         ┌─────────────────────┐
                         │    STAKEHOLDERS       │
                         └──────────┬──────────┘
              ┌──────────┬──────────┼──────────┬──────────┐
              ▼          ▼          ▼          ▼          ▼
        ┌──────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
        │ Business │ │  End   │ │ Legal &│ │ IT &   │ │  Ops   │
        │  Owners  │ │ Users  │ │Complian│ │Security│ │& DevOps│
        ├──────────┤ ├────────┤ ├────────┤ ├────────┤ ├────────┤
        │• ROI     │ │•Custom-│ │• HIPAA │ │• Threat│ │• Uptime│
        │• Revenue │ │ ers    │ │• PCI   │ │  model │ │• Deploy│
        │• Roadmap │ │•Staff/ │ │• GDPR  │ │• Pen   │ │• Scale │
        │• KPIs    │ │ Agents │ │• Audit │ │  test  │ │• Cost  │
        │• Timeline│ │•Partners││• Data  │ │• Access│ │• Infra │
        └──────────┘ └────────┘ └────────┘ └────────┘ └────────┘

        ↕ Each group produces or consumes specific artifacts:

        Business → BRD (Business Requirements Document)
        End Users → User stories, personas, journey maps
        Legal → Compliance checklist, data handling policies
        Security → Threat model, security requirements
        Ops → SLA requirements, infrastructure constraints
```

**Key Deliverables in this phase:**
- Business Requirements Document (BRD)
- Product Roadmap & MVP scope
- User personas and journey maps
- Regulatory/compliance checklist (HIPAA, PCI-DSS, GDPR)
- Budget & timeline estimates
- Risk assessment matrix
- Technical feasibility study

### 1.3 Requirements Gathering
w2
There are two kinds of requirements, and enterprise apps need BOTH thoroughly defined:

**Functional Requirements** = WHAT the app does  
**Non-Functional Requirements (NFRs)** = HOW WELL it does it

| Category | Banking Example | Healthcare Example | Finance/Trading Example |
|---|---|---|---|
| **Functional** | Fund transfer, bill pay, statements | Appointments, prescriptions, records | Portfolio view, trade execution, alerts |
| **Non-Functional** | <200ms API, 99.99% uptime | HIPAA compliance, audit trails | <50ms trade execution, real-time data |
| **Security** | 2FA, biometric, encryption at rest | PHI encryption, role-based access | End-to-end encryption, device binding |
| **Compliance** | PCI-DSS, SOX, KYC/AML | HIPAA, HITECH, HL7/FHIR | SEC regulations, SOX, FINRA |
| **Scale** | 10M+ users, 50K concurrent | 500K patients, 5K staff | 1M users, 100K concurrent during market hours |
| **Availability** | 24/7, <5 min downtime/month | Office hours critical, 24/7 ER | Market hours: zero downtime tolerance |

### 1.4 MVP Scoping — MoSCoW Method

Not everything can ship in v1. The MoSCoW method prioritizes features:

```
┌─────────────────────────────────────────────────────────────────────┐
│                   MoSCoW PRIORITIZATION                             │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  MUST HAVE (v1.0) — App doesn't work without these         │    │
│  │  • Login with 2FA          • Account balance                │    │
│  │  • Fund transfer            • Transaction history           │    │
│  │  • Push notifications       • Session management            │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  SHOULD HAVE (v1.1) — Important but can wait a sprint       │    │
│  │  • Bill payments            • Beneficiary management        │    │
│  │  • Mini statements          • Card controls (block/unblock) │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  COULD HAVE (v2.0) — Nice-to-have, enhances experience     │    │
│  │  • Spending analytics       • Budget tracking               │    │
│  │  • Biometric login          • Dark mode                     │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  WON'T HAVE (future) — Explicitly out of scope for now     │    │
│  │  • Investment/trading       • Insurance integration         │    │
│  │  • Loan application         • Chatbot                       │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 2: UI/UX DESIGN

> **Goal:** Design the user experience BEFORE writing code.
>
> Enterprise apps serve millions of users including elderly and disabled users.
> Poor UX in a banking app = users can't access their money = regulatory complaints.
> This is why design gets its own phase in enterprise development.

### 2.1 Design Process

```
┌─────────────────────────────────────────────────────────────────────┐
│                      UI/UX DESIGN PIPELINE                          │
│                                                                      │
│  ┌───────────┐    ┌───────────┐    ┌───────────┐    ┌───────────┐  │
│  │  User     │    │  Wire-    │    │  High-Fi  │    │ Interactive│  │
│  │  Research │───→│  frames   │───→│  Mockups  │───→│ Prototype │  │
│  │           │    │ (Lo-Fi)   │    │  (Figma)  │    │  (Figma)  │  │
│  └───────────┘    └───────────┘    └───────────┘    └─────┬─────┘  │
│                                                           │         │
│  Interviews,      Skeleton        Visual design,     Click-through  │
│  surveys,         layouts with    colors, fonts,     simulation     │
│  competitor       boxes and       icons, real        for usability  │
│  analysis         arrows          content            testing        │
│                                                           │         │
│                                                           ▼         │
│                                                    ┌───────────┐   │
│                                                    │ Usability │   │
│                                                    │  Testing  │   │
│                                                    │ (5+ users)│   │
│                                                    └─────┬─────┘   │
│                                                          │          │
│                                               Iterate if needed     │
│                                                          │          │
│                                                          ▼          │
│                                                    ┌───────────┐   │
│                                                    │  Design   │   │
│                                                    │  System   │   │
│                                                    │ Hand-off  │   │
│                                                    │ to Devs   │   │
│                                                    └───────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Design System

Enterprise apps build a **Design System** — a shared library of reusable UI
components, colors, typography, and spacing rules. This ensures consistency
across the entire app and speeds up development.

```
┌─────────────────────────────────────────────────────────────────┐
│                     DESIGN SYSTEM                                │
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │  TOKENS      │  │ COMPONENTS  │  │  PATTERNS               │ │
│  │             │  │             │  │                         │ │
│  │ Colors:     │  │ Buttons:    │  │ Login flow              │ │
│  │  primary    │  │  Primary    │  │ Form validation         │ │
│  │  secondary  │  │  Secondary  │  │ Error states            │ │
│  │  error      │  │  Text       │  │ Loading shimmer         │ │
│  │  surface    │  │  FAB        │  │ Pull-to-refresh         │ │
│  │             │  │             │  │ Empty states            │ │
│  │ Typography: │  │ Cards:      │  │ Bottom sheet            │ │
│  │  H1..H6    │  │  Account    │  │ Navigation pattern      │ │
│  │  Body       │  │  Transaction│  │ Confirmation dialogs    │ │
│  │  Caption    │  │  Alert      │  │ Success/error screens   │ │
│  │             │  │             │  │                         │ │
│  │ Spacing:    │  │ Inputs:     │  │ Accessibility:          │ │
│  │  4/8/16/24  │  │  TextField  │  │  Min touch: 48dp       │ │
│  │  dp grid    │  │  Dropdown   │  │  Color contrast 4.5:1  │ │
│  │             │  │  OTP Field  │  │  Screen reader labels  │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
│                                                                  │
│  Tool: Figma → Compose Theme (auto-export via plugins)          │
└─────────────────────────────────────────────────────────────────┘
```

---

## PHASE 3: ARCHITECTURE & SYSTEM DESIGN

> **Goal:** Define HOW the system will be built — the blueprint before construction.
>
> Architecture is the most critical decision phase. A wrong architecture choice for a
> banking app can mean months of rework. Enterprise architects consider: scalability
> (will it handle 10M users?), security (can it be hacked?), availability (what if
> a server dies?), and compliance (can we prove data access to regulators?).

### 3.1 High-Level System Architecture

This is the "30,000 foot view" — every enterprise app has these layers, regardless
of whether it's banking, healthcare, or finance:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            CLIENT LAYER                                 │
│   Users interact with the system through these frontends.              │
│                                                                         │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │
│   │ Android App  │    │   iOS App    │    │   Web App    │             │
│   │  (Kotlin /   │    │  (Swift /    │    │  (React /    │             │
│   │   Compose)   │    │  SwiftUI)    │    │   Next.js)   │             │
│   └──────┬───────┘    └──────┬───────┘    └──────┬───────┘             │
│          │                   │                   │                      │
│          │   HTTPS (TLS 1.3) │   + Certificate   │  Pinning            │
└──────────┼───────────────────┼───────────────────┼──────────────────────┘
           │                   │                   │
           ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         EDGE / GATEWAY LAYER                            │
│   First line of defense. All traffic enters through here.              │
│                                                                         │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                   CDN (CloudFront / Akamai)                    │   │
│   │         Static assets, images, DDoS protection                 │   │
│   └──────────────────────────┬─────────────────────────────────────┘   │
│                              ▼                                         │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                WAF (Web Application Firewall)                  │   │
│   │      SQL injection, XSS, bot protection, geo-blocking         │   │
│   └──────────────────────────┬─────────────────────────────────────┘   │
│                              ▼                                         │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │           API Gateway (Kong / AWS API Gateway / Apigee)        │   │
│   │                                                                │   │
│   │  • Authentication (JWT validation)                             │   │
│   │  • Rate limiting (100 req/min per user, 1000 req/min global)   │   │
│   │  • Request/response transformation                             │   │
│   │  • API versioning (/v1/, /v2/)                                 │   │
│   │  • Load balancing across service instances                     │   │
│   │  • Request logging & tracing (correlation IDs)                 │   │
│   └──────────────────────────┬─────────────────────────────────────┘   │
└──────────────────────────────┼──────────────────────────────────────────┘
                               │
┌──────────────────────────────┼──────────────────────────────────────────┐
│                      BACKEND SERVICES LAYER                             │
│   Business logic lives here. Each microservice owns its domain.        │
│                              │                                          │
│      ┌───────────┐   ┌──────┴──────┐   ┌─────────────┐  ┌──────────┐ │
│      │   Auth    │   │   Core      │   │ Notification │  │ Analytics│ │
│      │  Service  │   │  Business   │   │   Service    │  │ Service  │ │
│      │(Keycloak) │   │  Service    │   │  (Firebase)  │  │          │ │
│      └─────┬─────┘   └──────┬──────┘   └──────┬───────┘  └────┬─────┘ │
│            │                │                  │               │        │
│      ┌─────┴────────────────┴──────────────────┴───────────────┴───┐   │
│      │           Message Broker (Apache Kafka / RabbitMQ)          │   │
│      │                                                             │   │
│      │  Why? → Decouples services. If Notification service is      │   │
│      │  down, transactions still work. Events are queued and       │   │
│      │  processed when the service recovers (resilience).          │   │
│      └─────────────────────────┬───────────────────────────────────┘   │
└────────────────────────────────┼────────────────────────────────────────┘
                                 │
┌────────────────────────────────┼────────────────────────────────────────┐
│                          DATA LAYER                                     │
│   Different databases for different needs (polyglot persistence).      │
│                                 │                                       │
│   ┌──────────┐  ┌──────────┐   │   ┌──────────┐  ┌──────────────┐    │
│   │PostgreSQL│  │  Redis   │   │   │  MongoDB  │  │ Elasticsearch│    │
│   │(Primary  │  │ (Cache + │   │   │ (Audit   │  │  (Full-text  │    │
│   │ RDBMS,   │  │  Session │   │   │  logs,   │  │   search on  │    │
│   │ ACID)    │  │  Store)  │   │   │  flexible│  │  transactions│    │
│   └──────────┘  └──────────┘       │  schemas) │  │   and logs)  │    │
│                                    └──────────┘  └──────────────┘    │
│   ┌──────────┐  ┌───────────────┐                                    │
│   │   S3     │  │  Data         │  Why multiple databases?           │
│   │(Files:   │  │  Warehouse    │  → PostgreSQL: transactions need   │
│   │ KYC docs,│  │  (Redshift/   │    ACID guarantees (money!)        │
│   │ avatars) │  │   BigQuery)   │  → Redis: sub-millisecond lookups  │
│   └──────────┘  └───────────────┘  → MongoDB: flexible audit logs    │
│                                    → S3: cheap file storage           │
└────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Monolith → Microservices Decision

This is one of the most important architectural decisions. Here's the thought process:

```
┌─────────────────────────────────────────────────────────────────────┐
│            MONOLITH vs MICROSERVICES — DECISION MATRIX              │
│                                                                      │
│  Question                          Monolith    Microservices         │
│  ─────────────────────────────     ────────    ──────────────        │
│  Team size?                        < 15 devs   > 15 devs            │
│  Need independent deployments?      No          Yes                  │
│  Different scaling per feature?     No          Yes                  │
│  Multiple programming languages?    No          Yes                  │
│  Budget for infrastructure?         Limited     Healthy              │
│  Time to market?                    Faster      Slower initially     │
│  Operational complexity OK?         No          Yes (have DevOps)    │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │              THE EVOLUTION PATH                               │  │
│  │                                                               │  │
│  │  Phase 1        Phase 2            Phase 3                    │  │
│  │  (MVP)          (Growth)           (Scale)                    │  │
│  │                                                               │  │
│  │  ┌────────┐    ┌────────────┐     ┌──────┐ ┌──────┐ ┌─────┐ │  │
│  │  │        │    │ Modular    │     │ Auth │ │ Core │ │ Pay │ │  │
│  │  │Monolith│───→│ Monolith   │────→│ Svc  │ │ Svc  │ │ Svc │ │  │
│  │  │        │    │(well-      │     └──────┘ └──────┘ └─────┘ │  │
│  │  └────────┘    │ structured │     ┌──────┐ ┌──────┐         │  │
│  │                │ packages)  │     │Notif │ │Audit │  ...    │  │
│  │  ~3-6 months   └────────────┘     │ Svc  │ │ Svc  │         │  │
│  │                ~6-12 months       └──────┘ └──────┘         │  │
│  │                                   12-24 months               │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  Key Insight: Most successful enterprise apps START as a             │
│  well-structured modular monolith, then extract microservices        │
│  one by one as scale demands it. Don't start with microservices.     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 Android App Architecture (Clean Architecture + MVVM)

Why Clean Architecture for enterprise? Because it enables:
- **Testability:** Business logic has no Android dependencies → fast unit tests
- **Team scalability:** Multiple devs work on different layers without conflicts
- **Swappability:** Can replace Retrofit with Ktor without touching business logic

```
┌──────────────────────────────────────────────────────────────────┐
│                    ANDROID APP ARCHITECTURE                       │
│                    (Clean Architecture + MVVM)                    │
│                                                                   │
│  ╔═══════════════════════════════════════════════════════════╗    │
│  ║              PRESENTATION LAYER                           ║    │
│  ║                                                           ║    │
│  ║    ┌─────────────┐         ┌──────────────────┐          ║    │
│  ║    │  Composable  │◄───────│   ViewModel      │          ║    │
│  ║    │  Screens     │        │                  │          ║    │
│  ║    │              │        │  • Holds UiState │          ║    │
│  ║    │ • Observes   │ State  │  • Handles UI    │          ║    │
│  ║    │   UiState    │ Flow   │    events        │          ║    │
│  ║    │ • Sends user │───────►│  • Calls Use     │          ║    │
│  ║    │   events     │ Events │    Cases         │          ║    │
│  ║    └─────────────┘         └────────┬─────────┘          ║    │
│  ╚═════════════════════════════════════╪═════════════════════╝    │
│                                        │ depends on                │
│  ╔═════════════════════════════════════╪═════════════════════╗    │
│  ║              DOMAIN LAYER           │ (Pure Kotlin)       ║    │
│  ║                                     │                     ║    │
│  ║    ┌────────────────┐    ┌──────────▼─────────┐          ║    │
│  ║    │  Domain Models │    │     Use Cases      │          ║    │
│  ║    │                │    │                    │          ║    │
│  ║    │  • User        │    │  • LoginUseCase    │          ║    │
│  ║    │  • Account     │    │  • TransferUseCase │          ║    │
│  ║    │  • Transaction │    │  • GetBalanceUC    │          ║    │
│  ║    └────────────────┘    └──────────┬─────────┘          ║    │
│  ║                                     │                     ║    │
│  ║    ┌────────────────────────────────┐│                    ║    │
│  ║    │  Repository INTERFACES        ││ calls              ║    │
│  ║    │  (not implementations!)       │▼                    ║    │
│  ║    └────────────────────────────────┘                     ║    │
│  ╚═════════════════════════════════════╪═════════════════════╝    │
│                                        │ implemented by           │
│  ╔═════════════════════════════════════╪═════════════════════╗    │
│  ║              DATA LAYER             │                     ║    │
│  ║                                     │                     ║    │
│  ║    ┌────────────────────────────────▼──────────────────┐  ║    │
│  ║    │            Repository IMPL                        │  ║    │
│  ║    │     (Single Source of Truth pattern)               │  ║    │
│  ║    │                                                   │  ║    │
│  ║    │   Strategy: API → Cache in Room → Emit from Room  │  ║    │
│  ║    └───────┬────────────────┬────────────────┬─────────┘  ║    │
│  ║            │                │                │            ║    │
│  ║    ┌───────▼──────┐ ┌──────▼───────┐ ┌──────▼───────┐   ║    │
│  ║    │   Remote     │ │    Local     │ │  Preferences │   ║    │
│  ║    │  DataSource  │ │  DataSource  │ │  DataSource  │   ║    │
│  ║    │  (Retrofit)  │ │  (Room DB)   │ │  (DataStore) │   ║    │
│  ║    └──────────────┘ └─────────────┘ └──────────────┘   ║    │
│  ╚═══════════════════════════════════════════════════════════╝    │
│                                                                   │
│  KEY RULE: Dependencies point INWARD only.                        │
│  Domain layer knows NOTHING about Android, Retrofit, or Room.     │
│  Presentation knows nothing about how data is fetched.            │
└──────────────────────────────────────────────────────────────────┘
```

### 3.4 Data Flow Through the Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│              COMPLETE DATA FLOW: User Taps "Check Balance"           │
│                                                                       │
│  ①  User taps "Refresh" button                                       │
│      │                                                                │
│      ▼                                                                │
│  ②  Composable sends event → ViewModel                               │
│      │  onEvent(RefreshBalance)                                       │
│      │                                                                │
│      ▼                                                                │
│  ③  ViewModel calls → GetBalanceUseCase                              │
│      │  viewModelScope.launch { getBalance(accountId) }              │
│      │                                                                │
│      ▼                                                                │
│  ④  UseCase calls → AccountRepository (interface)                    │
│      │  repository.getAccountBalance(accountId)                      │
│      │                                                                │
│      ▼                                                                │
│  ⑤  Repository Impl → Executes strategy:                            │
│      │                                                                │
│      │  ┌─────────────────────────────────────────────────────┐     │
│      │  │  a) Emit cached balance from Room (instant UI)      │     │
│      │  │  b) Fetch fresh balance from API (background)       │     │
│      │  │  c) On API success → update Room cache              │     │
│      │  │  d) Room update triggers new Flow emission → UI     │     │
│      │  │  e) On API failure → keep showing cached data       │     │
│      │  │                       + show error snackbar         │     │
│      │  └─────────────────────────────────────────────────────┘     │
│      │                                                                │
│      ▼                                                                │
│  ⑥  ViewModel receives Flow<Balance> → maps to UiState              │
│      │  _uiState.value = UiState.Success(balance)                    │
│      │                                                                │
│      ▼                                                                │
│  ⑦  Composable recomposes with new balance                          │
│      │  Shows: "₹1,50,000.00" with last-updated timestamp           │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### 3.5 Communication Patterns Between Services

```
┌────────────────────────────────────────────────────────────────────┐
│         SYNCHRONOUS vs ASYNCHRONOUS COMMUNICATION                  │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  SYNCHRONOUS (REST / gRPC) — "I need an answer NOW"        │   │
│  │                                                             │   │
│  │  Transfer ──HTTP──→ Account Service                         │   │
│  │  Service   ←────── "Balance: $5000"                         │   │
│  │                                                             │   │
│  │  Used when: The caller CANNOT proceed without the response  │   │
│  │  Example: Checking balance before allowing a transfer       │   │
│  │  Risk: If Account Service is down, Transfer Service fails   │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  ASYNCHRONOUS (Events via Kafka) — "FYI, this happened"    │   │
│  │                                                             │   │
│  │  Transfer ──publish──→ Kafka Topic ──consume──→ Notif Svc  │   │
│  │  Service      "txn.completed"          │──────→ Audit Svc  │   │
│  │                                        └──────→ Analytics  │   │
│  │                                                             │   │
│  │  Used when: The caller does NOT need a response             │   │
│  │  Example: Sending notification after transfer completes     │   │
│  │  Benefit: If Notification Service is down, events queue up  │   │
│  │           and process when it recovers (no data loss)       │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Enterprise Rule of Thumb:                                          │
│  • 80% of inter-service communication should be ASYNC (events)     │
│  • 20% should be SYNC (only when you MUST have the answer now)     │
└────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 4: BACKEND DEVELOPMENT

> **Goal:** Build the server-side APIs and business logic that the Android app consumes.
>
> The Android app is just a "pretty face" — the real logic (transferring money,
> checking fraud, managing accounts) lives on the backend. Enterprise backends
> must be: highly available (99.99%), horizontally scalable, and auditable.
> A backend bug in a banking app could mean someone's money disappears.

### 4.1 Technology Stack Decision

The stack choice depends on the team's expertise, compliance requirements,
and ecosystem maturity. Here's what enterprises typically choose and WHY:

```
┌──────────────────────────────────────────────────────────────────────┐
│                     BACKEND TECH STACK DECISIONS                     │
├──────────────┬────────────────┬──────────────────────────────────────┤
│  Component   │    Options     │    Enterprise Pick & WHY             │
├──────────────┼────────────────┼──────────────────────────────────────┤
│  Language    │ Java, Kotlin,  │ Java/Kotlin (Spring Boot):           │
│              │ Go, Node.js,   │ Mature ecosystem, huge talent pool,  │
│              │ Python         │ proven at scale in banking for 20yr  │
│              │                │ Go: for latency-critical services    │
├──────────────┼────────────────┼──────────────────────────────────────┤
│  Framework   │ Spring Boot,   │ Spring Boot: battle-tested in       │
│              │ Ktor, Micronaut│ finance. Spring Security for auth.  │
│              │ Quarkus        │ Massive community & library support │
├──────────────┼────────────────┼──────────────────────────────────────┤
│  API Style   │ REST, GraphQL, │ REST: for mobile/web clients        │
│              │ gRPC           │ gRPC: between backend services      │
│              │                │ (10x faster, binary protocol)       │
├──────────────┼────────────────┼──────────────────────────────────────┤
│  Auth Server │ Keycloak, Auth0│ Keycloak: self-hosted (data stays   │
│              │ Okta, Custom   │ in your infrastructure — compliance) │
│              │                │ Auth0: SaaS, faster to set up       │
├──────────────┼────────────────┼──────────────────────────────────────┤
│  Messaging   │ Kafka, RabbitMQ│ Kafka: high-throughput event stream │
│              │ SQS, NATS      │ (100K+ events/sec), replay ability  │
│              │                │ RabbitMQ: simpler, lower throughput  │
├──────────────┼────────────────┼──────────────────────────────────────┤
│  Cloud       │ AWS, GCP, Azure│ AWS: most banking/fintech use       │
│              │ On-premise     │ Azure: healthcare (HIPAA built-in)  │
│              │                │ On-premise: highest security needs  │
└──────────────┴────────────────┴──────────────────────────────────────┘
```

### 4.2 Microservices Architecture (Banking Example)

Each microservice owns a **bounded context** (a DDD concept) — it's fully
responsible for one business domain and has its OWN database (no shared DB!):

```
                         ┌──────────────────────────┐
                         │      API Gateway          │
                         │    (Kong / Nginx)          │
                         │                            │
                         │  Routes by URL path:       │
                         │  /auth/*  → Auth Service   │
                         │  /acct/*  → Account Svc    │
                         │  /txn/*   → Transfer Svc   │
                         │  /cards/* → Card Service   │
                         └────────────┬───────────────┘
                                      │
           ┌──────────┬───────────────┼───────────────┬──────────┐
           ▼          ▼               ▼               ▼          ▼
     ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
     │   AUTH   │ │ ACCOUNT  │ │ TRANSFER │ │  CARDS   │ │  LOANS   │
     │ SERVICE  │ │ SERVICE  │ │ SERVICE  │ │ SERVICE  │ │ SERVICE  │
     │          │ │          │ │          │ │          │ │          │
     │ • Login  │ │ • Balance│ │ • P2P    │ │ • Issue  │ │ • Apply  │
     │ • 2FA    │ │ • History│ │ • NEFT   │ │ • Block  │ │ • Status │
     │ • OAuth  │ │ • KYC    │ │ • RTGS   │ │ • Limit  │ │ • EMI    │
     │ • Token  │ │ • Profile│ │ • UPI    │ │ • Reward │ │ • Docs   │
     │ • Biomet.│ │ • Stmt   │ │ • QR Pay │ │ • PIN    │ │ • Approve│
     └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
          │            │            │             │            │
     ┌────▼────┐  ┌────▼────┐ ┌────▼────┐  ┌────▼────┐  ┌────▼────┐
     │ Auth DB │  │ Acct DB │ │ Txn DB  │  │ Card DB │  │ Loan DB │
     │(Postgre)│  │(Postgre)│ │(Postgre)│  │(Postgre)│  │(Postgre)│
     └─────────┘  └─────────┘ └─────────┘  └─────────┘  └─────────┘
          │            │            │             │            │
          └────────────┴────────────┴─────────────┴────────────┘
                                    │
                                    ▼
     ┌──────────────────────────────────────────────────────────────┐
     │              EVENT BUS (Apache Kafka)                         │
     │                                                               │
     │  Topics:                                                      │
     │    auth.login.success     → Audit, Analytics                  │
     │    auth.login.failed      → Fraud Detection, Security         │
     │    txn.initiated          → Notification, Audit               │
     │    txn.completed          → Notification, Analytics, Rewards  │
     │    txn.failed             → Notification, Audit, Support      │
     │    card.blocked           → Notification, Fraud               │
     │    kyc.verified           → Account Activation                │
     └──────────────────────────────────────────────────────────────┘
                                    │
              ┌─────────────┬───────┼───────┬─────────────┐
              ▼             ▼       ▼       ▼             ▼
        ┌──────────┐  ┌─────────┐ ┌──────┐ ┌──────────┐ ┌──────────┐
        │Notificat.│  │  Audit  │ │Fraud │ │ Report & │ │ Rewards  │
        │ Service  │  │  Logger │ │Detect│ │ Analytics│ │ Service  │
        │          │  │         │ │(ML)  │ │          │ │          │
        │ Push,SMS │  │ Every   │ │Real- │ │ Grafana  │ │ Cashback │
        │ Email    │  │ action  │ │time  │ │ Dashbrd  │ │ Points   │
        └──────────┘  └─────────┘ └──────┘ └──────────┘ └──────────┘
```

### 4.3 API Design — REST Endpoints (Banking)

API design follows REST conventions. Enterprise APIs are **versioned** (v1, v2)
so old mobile app versions don't break when the API evolves:

```
Base URL: https://api.mybank.com/v1
Authentication: Bearer JWT token in Authorization header

┌────────────────────────────────────────────────────────────────────┐
│  AUTH ENDPOINTS                                                    │
│  POST   /auth/login              → Login (returns JWT pair)       │
│  POST   /auth/refresh            → Refresh access token           │
│  POST   /auth/otp/send           → Send OTP to phone/email       │
│  POST   /auth/otp/verify         → Verify OTP code               │
│  POST   /auth/biometric/enroll   → Register device biometric     │
│  POST   /auth/biometric/verify   → Verify biometric challenge    │
│  DELETE /auth/logout             → Invalidate session             │
├────────────────────────────────────────────────────────────────────┤
│  ACCOUNT ENDPOINTS                                                 │
│  GET    /accounts                → List user's accounts           │
│  GET    /accounts/{id}           → Account details                │
│  GET    /accounts/{id}/balance   → Real-time balance              │
│  GET    /accounts/{id}/statement → Mini statement (last 10 txns)  │
│  GET    /accounts/{id}/transactions?from=&to=&page=&size=&sort=   │
├────────────────────────────────────────────────────────────────────┤
│  TRANSFER ENDPOINTS                                                │
│  POST   /transfers               → Initiate transfer              │
│  GET    /transfers/{id}          → Transfer status                │
│  POST   /transfers/{id}/confirm  → Confirm with OTP              │
│  GET    /beneficiaries           → List saved beneficiaries       │
│  POST   /beneficiaries           → Add new beneficiary            │
│  DELETE /beneficiaries/{id}      → Remove beneficiary             │
├────────────────────────────────────────────────────────────────────┤
│  CARD ENDPOINTS                                                    │
│  GET    /cards                   → List user's cards              │
│  POST   /cards/{id}/block        → Block lost/stolen card        │
│  POST   /cards/{id}/unblock      → Unblock card                  │
│  PUT    /cards/{id}/limit        → Update daily limit            │
│  PUT    /cards/{id}/pin          → Change PIN (encrypted)        │
│  GET    /cards/{id}/transactions → Card-specific transactions    │
└────────────────────────────────────────────────────────────────────┘

Pagination Convention:
  GET /accounts/{id}/transactions?page=0&size=20&sort=date,desc

Response Envelope:
  {
    "status": "success",
    "data": { ... },
    "meta": { "page": 0, "size": 20, "total": 156 },
    "timestamp": "2026-04-24T10:30:00Z"
  }

Error Envelope:
  {
    "status": "error",
    "error": { "code": "INSUFFICIENT_FUNDS", "message": "..." },
    "timestamp": "2026-04-24T10:30:00Z"
  }
```

### 4.4 Backend Request Flow — Complete Journey

This shows what happens from the moment the Android app makes an API call
to the final response. Understanding this helps Android devs debug issues:

```
┌──────────────────────────────────────────────────────────────────────┐
│                COMPLETE REQUEST LIFECYCLE                             │
│                                                                       │
│  Android App                                                         │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │  OkHttp builds request:                                   │       │
│  │  POST /v1/transfers                                       │       │
│  │  Headers: Authorization: Bearer eyJhbG...                 │       │
│  │           X-Device-Id: abc123                             │       │
│  │           X-Request-Id: uuid-for-tracing                  │       │
│  │  Body: { "from": "ACC001", "to": "ACC002", "amount": 500}│       │
│  └──────────────────────────┬───────────────────────────────┘       │
│                             │ HTTPS (TLS 1.3)                        │
│                             ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │  API GATEWAY                                              │       │
│  │  ① SSL terminates here                                    │       │
│  │  ② Validate JWT signature + expiration                    │       │
│  │  ③ Check rate limit (user: 100/min, IP: 500/min)          │       │
│  │  ④ Attach correlation ID for distributed tracing          │       │
│  │  ⑤ Route: /transfers → Transfer Service cluster           │       │
│  └──────────────────────────┬───────────────────────────────┘       │
│                             ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │  TRANSFER SERVICE (one of 5 replicas)                     │       │
│  │                                                           │       │
│  │  Controller Layer:                                        │       │
│  │  ⑥ Deserialize request body                               │       │
│  │  ⑦ Validate input (amount > 0, accounts exist)            │       │
│  │                                                           │       │
│  │  Service Layer:                                           │       │
│  │  ⑧ gRPC call → Account Service: "Is balance >= 500?"     │       │
│  │  ⑨ gRPC call → Fraud Service: "Is this suspicious?"      │       │
│  │  ⑩ If all OK → Create transaction (status: PENDING)      │       │
│  │  ⑪ Debit source account (within DB transaction)           │       │
│  │  ⑫ Credit target account                                  │       │
│  │  ⑬ Update transaction status → COMPLETED                  │       │
│  │  ⑭ Publish Kafka event: "txn.completed"                   │       │
│  │                                                           │       │
│  │  Response: 201 Created                                    │       │
│  │  { "transactionId": "TXN-2026-04-24-0001",               │       │
│  │    "status": "COMPLETED", "amount": 500 }                │       │
│  └──────────────────────────┬───────────────────────────────┘       │
│                             │                                        │
│                             │  Kafka Event (async, fire-and-forget) │
│                             ▼                                        │
│  ┌──────────────────────────────────────────────────────────┐       │
│  │  DOWNSTREAM EVENT CONSUMERS (process independently)       │       │
│  │                                                           │       │
│  │  Notification Service → Push: "₹500 sent to Account002" │       │
│  │  Audit Logger         → Store immutable audit record      │       │
│  │  Analytics Service    → Update daily transfer metrics     │       │
│  │  Fraud ML Pipeline    → Feed model for anomaly detection  │       │
│  │  Rewards Service      → Credit loyalty points             │       │
│  └──────────────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 5: DATABASE DESIGN

> **Goal:** Design how data is stored, accessed, and protected.
>
> In enterprise apps, the database is the most critical component. If the app
> crashes, users re-open it. If the database loses data, users lose their money
> or medical records. That's why enterprise databases use ACID transactions,
> multi-region replication, point-in-time recovery, and encryption at rest.

### 5.1 Database Strategy — Polyglot Persistence

"Polyglot persistence" means using the RIGHT database for each job, instead of
forcing one database to do everything:

```
┌───────────────────────────────────────────────────────────────────────┐
│                  DATABASE POLYGLOT PERSISTENCE                        │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │  PostgreSQL (Primary — Relational)                           │    │
│   │  • Core business data: accounts, transactions, users         │    │
│   │  • WHY: ACID compliance (money MUST be consistent)           │    │
│   │  • ACID = Atomicity, Consistency, Isolation, Durability      │    │
│   │  • If debit succeeds but credit fails → BOTH roll back      │    │
│   └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │  Redis (In-Memory Cache)                                     │    │
│   │  • Session tokens, OTP codes (with TTL auto-expiry)          │    │
│   │  • Rate limiting counters, real-time leaderboards            │    │
│   │  • WHY: Sub-millisecond reads (vs 5-50ms for PostgreSQL)    │    │
│   │  • OTP stored with TTL=300s → auto-deletes after 5 minutes  │    │
│   └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │  MongoDB (Document Store)                                    │    │
│   │  • Audit logs, configuration, flexible/evolving schemas      │    │
│   │  • WHY: Audit logs have different shapes per action type     │    │
│   │  • Immutable: logs are APPEND-ONLY (compliance requirement) │    │
│   └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │  Elasticsearch (Search Engine)                               │    │
│   │  • Full-text search on transactions, logs, customer support  │    │
│   │  • WHY: "Find all transactions containing 'Amazon' in the   │    │
│   │    last 30 days" — PostgreSQL LIKE is too slow at scale      │    │
│   └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │  S3 / Blob Storage                                           │    │
│   │  • KYC documents, generated PDF statements, avatars          │    │
│   │  • WHY: Cheap, infinitely scalable, 99.999999999% durable   │    │
│   └──────────────────────────────────────────────────────────────┘    │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐    │
│   │  Room (Android — On-Device)                                  │    │
│   │  • Offline cache of accounts, recent transactions            │    │
│   │  • WHY: App works without internet, instant UI               │    │
│   │  • NOT source of truth — server is always authoritative      │    │
│   └──────────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────────────┘
```

### 5.2 Schema Design (Banking — Server-Side)

Relational schema follows **normalization** rules to avoid data duplication
and maintain referential integrity. Foreign keys ensure you can't create
a transaction for a non-existent account.

```
┌────────────────────┐         ┌────────────────────┐
│      users         │         │     accounts       │
├────────────────────┤         ├────────────────────┤
│ id (PK, UUID)      │────┐    │ id (PK, UUID)      │
│ email (unique)     │    │    │ user_id (FK) ◄─────┤───┐
│ phone (unique)     │    │    │ account_number     │   │
│ password_hash      │    │    │  (unique, indexed) │   │
│ kyc_status         │    │    │ type (enum:        │   │
│  (PENDING/DONE)    │    │    │  SAVINGS/CURRENT)  │   │
│ is_2fa_enabled     │    │    │ balance (decimal)  │   │
│ status (ACTIVE/    │    │    │ currency (INR/USD) │   │
│  BLOCKED/CLOSED)   │    │    │ status             │   │
│ created_at         │    │    │ opened_at          │   │
│ updated_at         │    │    │ last_txn_at        │   │
│ last_login_at      │    │    └────────────────────┘   │
└────────────────────┘    │                              │
                          │    ┌────────────────────┐    │
                          │    │   transactions     │    │
                          │    ├────────────────────┤    │
                          │    │ id (PK, UUID)      │    │
                          │    │ reference_id       │    │
                          │    │  (unique, idempo-  │    │
                          │    │   tency key)       │    │
                          │    │ from_account (FK)──┤────┘
                          │    │ to_account (FK)────┤────┘
                          │    │ amount (decimal)   │
                          │    │ currency           │
                          │    │ type (enum: P2P/   │
                          │    │  NEFT/RTGS/UPI)    │
                          │    │ status (enum:      │
                          │    │  PENDING/COMPLETED │
                          │    │  /FAILED/REVERSED) │
                          │    │ description        │
                          │    │ created_at (index) │
                          │    └────────────────────┘
                          │
                          │    ┌────────────────────┐
                          │    │   beneficiaries    │
                          │    ├────────────────────┤
                          └───►│ id (PK)            │
                               │ user_id (FK)       │
                               │ nickname           │
                               │ account_number     │
                               │ ifsc_code          │
                               │ bank_name          │
                               │ is_verified        │
                               │ verified_at        │
                               │ created_at         │
                               └────────────────────┘

┌────────────────────┐         ┌────────────────────┐
│      cards         │         │   devices          │
├────────────────────┤         ├────────────────────┤
│ id (PK)            │         │ id (PK)            │
│ account_id (FK)    │         │ user_id (FK)       │
│ card_number_hash   │         │ device_id          │
│ card_token         │         │ device_name        │
│  (tokenized, not   │         │ os_version         │
│   actual number!)  │         │ app_version        │
│ card_type          │         │ push_token (FCM)   │
│ expiry_month       │         │ is_trusted         │
│ expiry_year        │         │ last_active_at     │
│ daily_limit        │         │ registered_at      │
│ status             │         └────────────────────┘
│ created_at         │
└────────────────────┘         ┌────────────────────┐
                               │  audit_logs        │
NOTE: Card numbers              ├────────────────────┤
are NEVER stored in              │ id (PK)            │
plain text. Only a               │ user_id            │
tokenized reference              │ action (LOGIN/     │
is stored (PCI-DSS               │  TRANSFER/VIEW)    │
requirement).                    │ resource           │
                               │ ip_address         │
                               │ device_id          │
                               │ geo_location       │
                               │ request_summary    │
                               │ response_code      │
                               │ risk_score         │
                               │ timestamp (index)  │
                               └────────────────────┘
```

### 5.3 Android Local Database (Room) — Offline Cache

The Android app maintains a **read-only cache** of server data. It's used
to show data instantly while a fresh API call happens in the background:

```
┌──────────────────────────────────────────────────────────────────────┐
│                 ANDROID LOCAL DB (Room)                               │
│                                                                       │
│  Purpose: Offline cache for fast UI. Server is ALWAYS the truth.     │
│                                                                       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐   │
│  │  cached_accounts │  │  cached_         │  │  pending_       │   │
│  │                  │  │  transactions    │  │  operations     │   │
│  │  • id (PK)      │  │                  │  │                 │   │
│  │  • acct_number  │  │  • id (PK)       │  │  • id (PK)     │   │
│  │  • balance      │  │  • account_id    │  │  • type        │   │
│  │  • type         │  │  • amount        │  │  • payload     │   │
│  │  • currency     │  │  • type          │  │    (JSON)      │   │
│  │  • status       │  │  • date          │  │  • status      │   │
│  │  • last_synced  │  │  • description   │  │    (QUEUED/    │   │
│  │    _at          │  │  • last_synced   │  │    SYNCING/    │   │
│  └──────────────────┘  │    _at          │  │    SYNCED/     │   │
│                        └──────────────────┘  │    FAILED)     │   │
│                                              │  • retry_count │   │
│  ┌──────────────────┐                        │  • created_at  │   │
│  │  user_prefs      │                        └─────────────────┘   │
│  │  (DataStore)     │                                              │
│  │                  │  The "pending_operations" table enables       │
│  │  • theme         │  offline writes. When the user transfers     │
│  │  • biometric_on │  money while offline, the operation is        │
│  │  • notif_prefs  │  queued and synced via WorkManager when       │
│  │  • last_sync    │  connectivity is restored.                    │
│  └──────────────────┘                                              │
└──────────────────────────────────────────────────────────────────────┘

Sync Strategy:

  ┌─────────┐    ┌─────────────┐    ┌──────────────┐    ┌──────────┐
  │ App     │    │ Show cached │    │ Fetch fresh  │    │ Update   │
  │ Launch  │───→│ data from   │───→│ from API     │───→│ Room DB  │
  │         │    │ Room (fast) │    │ (background) │    │ (cache)  │
  └─────────┘    └─────────────┘    └──────────────┘    └────┬─────┘
                                                              │
                                                              ▼
                                                    ┌──────────────┐
                                                    │ Room emits   │
                                                    │ new data via │
                                                    │ Flow → UI   │
                                                    │ auto-updates │
                                                    └──────────────┘

  Stale Policy: Data older than 5 min → auto-refresh on view
  Force Refresh: Pull-to-refresh gesture
  Offline Write: Queue → WorkManager → Sync when online
```

---

## PHASE 6: ANDROID APP DEVELOPMENT

> **Goal:** Build the Android client that users interact with.
>
> Enterprise Android apps differ from personal projects in three key ways:
> 1. **Multi-module** — Code is split across modules for faster builds and team isolation
> 2. **Offline-first** — Must work without internet (show cached data, queue writes)
> 3. **Security-hardened** — Root detection, certificate pinning, encrypted storage

### 6.1 Project Setup — Multi-Module Architecture

Monolithic `:app` modules don't scale when 10+ developers work on the same codebase.
Multi-module architecture gives: faster build times (Gradle caches unchanged modules),
enforced boundaries (feature modules can't access each other directly), and
parallel development (teams own entire modules).

```
┌─────────────────────────────────────────────────────────────────┐
│                    MODULE STRUCTURE                               │
│                                                                  │
│  :app                          → Shell: wires everything together│
│  │                                                               │
│  ├── :core                                                       │
│  │    ├── :core:network        → Retrofit, OkHttp, interceptors │
│  │    ├── :core:database       → Room DB, DAOs, entities, migr. │
│  │    ├── :core:common         → Extensions, utils, constants    │
│  │    ├── :core:ui             → Design system: theme, shared   │
│  │    │                          composables, icons, colors      │
│  │    ├── :core:security       → Encryption, biometric, keystore│
│  │    ├── :core:datastore      → DataStore (user preferences)   │
│  │    └── :core:testing        → Shared test utilities, fakes   │
│  │                                                               │
│  └── :feature                                                    │
│       ├── :feature:auth        → Login, signup, OTP, biometric  │
│       ├── :feature:home        → Dashboard, quick actions       │
│       ├── :feature:accounts    → Account list, details, stmt    │
│       ├── :feature:transfers   → Send money, beneficiaries      │
│       ├── :feature:cards       → Card controls, transactions    │
│       ├── :feature:payments    → Bill pay, QR pay, UPI          │
│       ├── :feature:profile     → Settings, preferences, KYC    │
│       └── :feature:notifications → Notification center          │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Module Dependency Rules

Strict rules prevent spaghetti dependencies. These are enforced via Gradle config:

```
                              :app
                               │
                               │ depends on ALL feature modules
           ┌───────┬───────┬───┼───┬───────┬───────┐
           ▼       ▼       ▼   ▼   ▼       ▼       ▼
       :feature: :feature: :feature: :feature: :feature:
        auth     home     accounts transfers  cards  ...
           │       │       │       │       │
           │       │       │       │       │
           │  RULE: Feature modules NEVER depend on each other!
           │  They communicate via Navigation routes only.
           │       │       │       │       │
           └───────┴───────┼───────┴───────┘
                           │
                           │ ALL features depend on core modules
                ┌──────────┼──────────┐
                ▼          ▼          ▼
           :core:      :core:     :core:
           network     database   common
           :core:ui    :core:     :core:
                       datastore  security

  DEPENDENCY DIRECTION: app → feature → core (NEVER reverse)

  Why? If :feature:auth depended on :feature:home, changing
  home could break auth. With this structure, each feature
  is an isolated island connected only through shared :core.
```

### 6.3 Technology Stack

```
┌─────────────────────────────────────────────────────────────────┐
│                 ANDROID TECH STACK                               │
├──────────────┬──────────────────────────────────────────────────┤
│ Language     │ Kotlin 2.0 (100% Kotlin, no Java)               │
│ UI           │ Jetpack Compose + Material 3                    │
│ Navigation   │ Compose Navigation (type-safe args)             │
│ DI           │ Hilt (compile-time, Dagger under the hood)      │
│ Networking   │ Retrofit 2 + OkHttp 4 + Kotlinx Serialization  │
│ Local DB     │ Room (with migration strategies)                │
│ Preferences  │ DataStore (Proto for typed, Prefs for simple)   │
│ Async        │ Coroutines + Flow + StateFlow                   │
│ Image        │ Coil (Compose-native)                           │
│ Security     │ EncryptedSharedPreferences, Android Keystore    │
│ Biometric    │ AndroidX Biometric (BiometricPrompt)            │
│ Push         │ Firebase Cloud Messaging (FCM)                  │
│ Analytics    │ Firebase Analytics + Mixpanel                   │
│ Crash Report │ Firebase Crashlytics                            │
│ Background   │ WorkManager (sync, upload tasks)                │
│ Testing      │ JUnit5, Turbine, MockK, Compose Testing        │
│ Build        │ Gradle KTS + Version Catalogs (libs.versions)   │
│ Code Quality │ Detekt (static analysis), ktlint (formatting)  │
│ Feature Flags│ Firebase Remote Config                          │
└──────────────┴──────────────────────────────────────────────────┘
```

### 6.4 Screen Flow (Banking App)

This map shows every screen and how users navigate between them:

```
┌──────────────────────────────────────────────────────────────────────┐
│                     BANKING APP SCREEN FLOW                          │
│                                                                       │
│  ┌─────────┐     ┌──────────┐     ┌──────────────┐                  │
│  │ Splash  │────→│  Login   │────→│   OTP /      │                  │
│  │ Screen  │     │  Screen  │     │  Biometric   │                  │
│  │         │     │          │     │  Verification│                  │
│  │ Check:  │     │ Email +  │     │              │                  │
│  │ token   │     │ Password │     │ 6-digit OTP  │                  │
│  │ valid?  │     │          │     │ or fingerprnt│                  │
│  └────┬────┘     └──────────┘     └──────┬───────┘                  │
│       │                                   │                          │
│       │ (token valid, skip login)        │                          │
│       └──────────────┬───────────────────┘                          │
│                      ▼                                               │
│            ┌──────────────────┐                                      │
│            │    DASHBOARD     │                                      │
│            │     (Home)       │                                      │
│            │                  │                                      │
│            │ • Total balance  │                                      │
│            │ • Quick actions  │                                      │
│            │ • Recent txns    │                                      │
│            │ • Promo banners  │                                      │
│            └────────┬─────────┘                                      │
│                     │                                                │
│    ┌────────┬───────┼───────┬─────────┬──────────┐                  │
│    ▼        ▼       ▼       ▼         ▼          ▼                  │
│ ┌────────┐┌──────┐┌──────┐┌────────┐┌────────┐┌────────┐          │
│ │Accounts││Trans-││Cards ││Pay     ││Invest  ││Profile │          │
│ │ List   ││fer   ││      ││Bills   ││(Future)││Settings│          │
│ └───┬────┘└──┬───┘└──┬───┘└───┬────┘└────────┘└───┬────┘          │
│     │        │       │        │                    │                 │
│     ▼        ▼       ▼        ▼                    ▼                 │
│ ┌────────┐┌──────┐┌──────┐┌────────┐         ┌────────┐           │
│ │Account ││Select││Card  ││Select  │         │Personal│           │
│ │Details ││Benef.││Detail││Biller  │         │ Info   │           │
│ │        ││+     ││      ││+      │         │KYC     │           │
│ │Balance ││Amount││Block/││Amount  │         │Devices │           │
│ │History ││      ││Limit ││       │         │Security│           │
│ │Mini    ││      ││PIN   ││       │         │Logout  │           │
│ │Stmt    ││      ││Txns  ││       │         │        │           │
│ └────────┘└──┬───┘└──────┘└───┬────┘         └────────┘           │
│              │                │                                     │
│              ▼                ▼                                     │
│         ┌─────────┐     ┌─────────┐                                │
│         │ Review  │     │ Review  │                                │
│         │ & Confirm│    │ & Confirm│                               │
│         └────┬────┘     └────┬────┘                                │
│              ▼               ▼                                     │
│         ┌─────────┐     ┌─────────┐                                │
│         │  Enter  │     │  Enter  │                                │
│         │  OTP    │     │  OTP    │                                │
│         └────┬────┘     └────┬────┘                                │
│              ▼               ▼                                     │
│         ┌─────────┐     ┌─────────┐                                │
│         │ Success │     │ Success │                                │
│         │ Receipt │     │ Receipt │                                │
│         │ (Share) │     │ (Share) │                                │
│         └─────────┘     └─────────┘                                │
│                                                                     │
│  Bottom Navigation: Home | Accounts | Transfers | Cards | More     │
└──────────────────────────────────────────────────────────────────────┘
```

### 6.5 Offline-First Architecture

Enterprise apps MUST work offline because:
- Users might be in areas with poor connectivity
- Banking regulators require the app to at least SHOW balance info offline
- Network calls are slow; showing cached data first feels instant

```
┌──────────────────────────────────────────────────────────────────────┐
│                   OFFLINE-FIRST PATTERN                               │
│                                                                       │
│    User Opens App / Navigates to Screen                              │
│         │                                                             │
│         ▼                                                             │
│    ┌─────────────────────────────────────────┐                       │
│    │  Repository: getAccountBalance(id)      │                       │
│    └──────────────────┬──────────────────────┘                       │
│                       │                                               │
│         ┌─────────────┼─────────────┐                                │
│         ▼             ▼             ▼                                │
│    ┌─────────┐  ┌──────────┐  ┌──────────┐                          │
│    │ STEP 1  │  │ STEP 2   │  │ STEP 3   │                          │
│    │         │  │          │  │          │                          │
│    │ Emit    │  │ Fetch    │  │ Save API │                          │
│    │ cached  │  │ from API │  │ data to  │                          │
│    │ data    │  │(parallel)│  │ Room DB  │                          │
│    │ from    │  │          │  │          │                          │
│    │ Room DB │  │          │  │ Room     │                          │
│    │         │  │          │  │ emits    │                          │
│    │ User    │  │          │  │ updated  │                          │
│    │ sees    │  │          │  │ data via │                          │
│    │ data    │  │          │  │ Flow     │                          │
│    │ INSTANT │  │          │  │          │                          │
│    └─────────┘  └────┬─────┘  └──────────┘                          │
│                      │                                               │
│                 ┌────┴────┐                                          │
│                 │ Network │                                          │
│                 │ Status? │                                          │
│                 └────┬────┘                                          │
│              ┌───────┼───────┐                                      │
│              ▼               ▼                                      │
│         ┌─────────┐    ┌──────────┐                                 │
│         │ ONLINE  │    │ OFFLINE  │                                 │
│         │         │    │          │                                 │
│         │ Fetch + │    │ Show     │                                 │
│         │ update  │    │ cached   │                                 │
│         │ cache   │    │ data +   │                                 │
│         │         │    │ "Offline"│                                 │
│         │ UI auto-│    │ banner   │                                 │
│         │ updates │    │          │                                 │
│         │ via Flow│    │ Queue    │                                 │
│         └─────────┘    │ writes   │                                 │
│                        │ in Room  │                                 │
│                        └─────┬────┘                                 │
│                              │                                      │
│                              ▼                                      │
│                   ┌────────────────────┐                            │
│                   │    WorkManager     │                            │
│                   │                    │                            │
│                   │ Monitors network.  │                            │
│                   │ When online →      │                            │
│                   │ syncs pending ops  │                            │
│                   │ to server.         │                            │
│                   │                    │                            │
│                   │ Retry: exponential │                            │
│                   │ backoff (1s, 2s,   │                            │
│                   │ 4s, 8s...)         │                            │
│                   └────────────────────┘                            │
│                                                                      │
│  CONFLICT RESOLUTION:                                                │
│  • Read conflicts: Server data always wins (last-write-wins)        │
│  • Write conflicts: Server rejects if version mismatch              │
│    (optimistic locking via ETag/version field)                      │
│  • Financial transactions: NEVER cached offline — always online     │
│    (you can't transfer money without server confirmation)           │
└──────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 7: API INTEGRATION & DATA FLOW

> **Goal:** Connect the Android app to the backend via a robust, secure network layer.
>
> The network layer is the "bridge" between the beautiful Compose UI and the
> powerful backend. In enterprise apps, this layer does far more than just
> making HTTP calls — it handles authentication, retries, caching, offline
> support, and security (SSL pinning). A well-built network layer is the
> difference between an app that "works" and one that's production-ready.

### 7.1 Network Layer Architecture

Every HTTP request passes through a chain of interceptors before reaching
the server. Think of interceptors like middleware — each one adds something:

```
┌──────────────────────────────────────────────────────────────────────┐
│                  NETWORK LAYER — DETAILED STACK                      │
│                                                                       │
│   ViewModel calls UseCase → UseCase calls Repository                 │
│                                    │                                  │
│                                    ▼                                  │
│                            Repository Impl                            │
│                                    │                                  │
│                                    ▼                                  │
│   ┌────────────────────────────────────────────────────────────────┐ │
│   │                Retrofit Interface                               │ │
│   │                                                                │ │
│   │   @POST("transfers")                                           │ │
│   │   suspend fun transfer(@Body req: TransferReq): TransferRes    │ │
│   │                                                                │ │
│   │   Retrofit does:                                               │ │
│   │   1. Serialize Kotlin object → JSON (Kotlinx Serialization)   │ │
│   │   2. Build HTTP request object                                 │ │
│   │   3. Hand off to OkHttp for actual network call                │ │
│   │   4. Deserialize JSON response → Kotlin object                │ │
│   └────────────────────────────┬───────────────────────────────────┘ │
│                                │                                      │
│   ┌────────────────────────────┼───────────────────────────────────┐ │
│   │                   OkHttp Client                                │ │
│   │                            │                                   │ │
│   │   ┌───────────────────────────────────────────────────────┐   │ │
│   │   │              INTERCEPTOR CHAIN                        │   │ │
│   │   │              (Executed in order)                       │   │ │
│   │   │                                                       │   │ │
│   │   │  ┌─────────────────────────────────────────────────┐ │   │ │
│   │   │  │ 1. AuthInterceptor                              │ │   │ │
│   │   │  │    Attaches JWT: Authorization: Bearer eyJ...   │ │   │ │
│   │   │  │    If 401 → auto-refresh token → retry request  │ │   │ │
│   │   │  └─────────────────────────────────────────────────┘ │   │ │
│   │   │  ┌─────────────────────────────────────────────────┐ │   │ │
│   │   │  │ 2. DeviceInfoInterceptor                        │ │   │ │
│   │   │  │    Adds: X-Device-Id, X-App-Version, X-OS       │ │   │ │
│   │   │  │    Backend uses this for device trust scoring    │ │   │ │
│   │   │  └─────────────────────────────────────────────────┘ │   │ │
│   │   │  ┌─────────────────────────────────────────────────┐ │   │ │
│   │   │  │ 3. RequestIdInterceptor                         │ │   │ │
│   │   │  │    Adds: X-Request-Id: UUID (for tracing)       │ │   │ │
│   │   │  │    Used to trace request across all services     │ │   │ │
│   │   │  └─────────────────────────────────────────────────┘ │   │ │
│   │   │  ┌─────────────────────────────────────────────────┐ │   │ │
│   │   │  │ 4. LoggingInterceptor (DEBUG builds only!)      │ │   │ │
│   │   │  │    Logs request/response for debugging           │ │   │ │
│   │   │  │    NEVER in production (leaks sensitive data)    │ │   │ │
│   │   │  └─────────────────────────────────────────────────┘ │   │ │
│   │   │  ┌─────────────────────────────────────────────────┐ │   │ │
│   │   │  │ 5. CertificatePinner (CRITICAL for enterprise)  │ │   │ │
│   │   │  │    Pins server's SSL certificate hash            │ │   │ │
│   │   │  │    Prevents MITM attacks even on compromised     │ │   │ │
│   │   │  │    networks. If cert doesn't match → reject.     │ │   │ │
│   │   │  └─────────────────────────────────────────────────┘ │   │ │
│   │   └───────────────────────────────────────────────────────┘   │ │
│   │                            │                                   │ │
│   │   Connection Pool: Reuses TCP connections (saves handshake)   │ │
│   │   Timeouts: Connect=10s, Read=30s, Write=30s                 │ │
│   │   Retry: Auto-retry on connection failure (configurable)      │ │
│   └────────────────────────────┬───────────────────────────────────┘ │
│                                │                                      │
│                                ▼                                      │
│                         HTTPS → Server                                │
│                    (TLS 1.3 encrypted)                                │
└──────────────────────────────────────────────────────────────────────┘
```

### 7.2 Token Refresh Flow — The Authenticator Pattern

This is critical for enterprise apps. JWT access tokens are short-lived (15 min)
for security. When they expire, the app must silently refresh without
interrupting the user:

```
┌──────────────────────────────────────────────────────────────────────┐
│                   JWT TOKEN LIFECYCLE                                 │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  After successful login, server returns TWO tokens:            │  │
│  │                                                                │  │
│  │  access_token:  eyJhbGciOi...  (short-lived: 15 minutes)     │  │
│  │  refresh_token: eyJyZWZyZX...  (long-lived: 30 days)         │  │
│  │                                                                │  │
│  │  access_token  → Used for every API call                      │  │
│  │  refresh_token → Used ONLY to get a new access_token          │  │
│  │                                                                │  │
│  │  Storage: Both stored in EncryptedSharedPreferences            │  │
│  │           (AES-256 encryption via Android Keystore)            │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  Normal API Call:                                                    │
│                                                                       │
│  App ──[GET /accounts + Bearer access_token]──→ Server              │
│       │                                                              │
│       ├──→ 200 OK → ✅ Return data to UI                            │
│       │                                                              │
│       └──→ 401 Unauthorized (token expired!)                        │
│              │                                                       │
│              ▼                                                       │
│      ┌────────────────────────────────────────────────┐             │
│      │  OkHttp Authenticator (automatic, transparent)  │             │
│      │                                                 │             │
│      │  1. Take refresh_token from encrypted storage   │             │
│      │  2. POST /auth/refresh { refresh_token: "..." } │             │
│      │  3. Receive new access_token + refresh_token    │             │
│      │  4. Store new tokens in encrypted storage       │             │
│      │  5. Retry ORIGINAL request with new token       │             │
│      │  6. User never notices anything happened!       │             │
│      └───────────────────────┬─────────────────────────┘             │
│                              │                                       │
│              ┌───────────────┼───────────────┐                      │
│              ▼                               ▼                      │
│         ┌─────────┐                    ┌──────────┐                 │
│         │ 200 OK  │                    │ 401 AGAIN│                 │
│         │         │                    │          │                 │
│         │ Retry   │                    │ Refresh  │                 │
│         │ worked! │                    │ token    │                 │
│         │ Return  │                    │ ALSO     │                 │
│         │ data    │                    │ expired  │                 │
│         └─────────┘                    │          │                 │
│                                        │ → FORCE  │                 │
│                                        │   LOGOUT │                 │
│                                        │ → Clear  │                 │
│                                        │   all    │                 │
│                                        │   tokens │                 │
│                                        │ → Go to  │                 │
│                                        │   Login  │                 │
│                                        └──────────┘                 │
│                                                                      │
│  Thread Safety: Use Mutex/synchronized block to ensure only ONE     │
│  refresh happens at a time (avoid multiple concurrent refreshes)    │
└──────────────────────────────────────────────────────────────────────┘
```

### 7.3 Error Handling Strategy

Enterprise apps must handle errors gracefully — a user staring at a blank
screen or a cryptic "Error 500" is unacceptable:

```
┌──────────────────────────────────────────────────────────────────────┐
│                ERROR HANDLING STRATEGY                                │
│                                                                       │
│  HTTP Code   │  Meaning            │  App Behavior                  │
│  ────────────┼─────────────────────┼────────────────────────────────│
│  200-299     │  Success            │  Show data                     │
│  400         │  Bad request        │  Show validation errors        │
│  401         │  Unauthorized       │  Refresh token or force logout │
│  403         │  Forbidden          │  "You don't have permission"   │
│  404         │  Not found          │  "Account not found"           │
│  409         │  Conflict           │  "Transaction already exists"  │
│  422         │  Validation failed  │  Show field-level errors       │
│  429         │  Rate limited       │  Retry after X seconds         │
│  500         │  Server error       │  "Something went wrong" +      │
│              │                     │  retry button                   │
│  502-504     │  Gateway/timeout    │  "Server temporarily down" +   │
│              │                     │  auto-retry with backoff        │
│  No network  │  No internet        │  Show cached data + offline    │
│              │                     │  banner                         │
│                                                                       │
│  Error Response Model:                                               │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  sealed class NetworkResult<T> {                            │    │
│  │      data class Success<T>(val data: T)                     │    │
│  │      data class Error(val code: Int, val message: String)   │    │
│  │      data class NetworkError(val exception: IOException)    │    │
│  │  }                                                          │    │
│  └─────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 8: SECURITY & COMPLIANCE

> **Goal:** Protect user data, prevent fraud, and satisfy regulators.
>
> Security is NOT optional in enterprise apps — it's a legal requirement.
> A data breach in a banking app can result in millions in fines, lawsuits,
> and permanent loss of user trust. Enterprise apps implement "defense in depth"
> — multiple layers of security so that if one layer is breached, others still protect.

### 8.1 Security Architecture — Defense in Depth

Each layer adds protection. An attacker must breach ALL layers to succeed:

```
┌────────────────────────────────────────────────────────────────────┐
│             SECURITY LAYERS (Defense in Depth)                     │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  LAYER 1: NETWORK / TRANSPORT SECURITY                      │  │
│  │                                                              │  │
│  │  ┌────────────┐  ┌──────────────────┐  ┌──────────────┐    │  │
│  │  │  TLS 1.3   │  │ Certificate      │  │  No HTTP     │    │  │
│  │  │  (HTTPS)   │  │ Pinning (OkHttp) │  │  Fallback    │    │  │
│  │  │            │  │                  │  │              │    │  │
│  │  │ Encrypts   │  │ Pins server cert │  │ android:     │    │  │
│  │  │ all data   │  │ hash in app.     │  │ usesClear-   │    │  │
│  │  │ in transit │  │ Even if attacker │  │ textTraffic  │    │  │
│  │  │            │  │ has a fake cert, │  │ = "false"    │    │  │
│  │  │            │  │ app rejects it.  │  │              │    │  │
│  │  └────────────┘  └──────────────────┘  └──────────────┘    │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  LAYER 2: AUTHENTICATION & SESSION MANAGEMENT               │  │
│  │                                                              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │  │
│  │  │ OAuth 2.0    │  │ Multi-Factor │  │ Device Binding   │  │  │
│  │  │ + PKCE       │  │ Auth (MFA)   │  │                  │  │  │
│  │  │              │  │              │  │ Token tied to    │  │  │
│  │  │ PKCE prevents│  │ Password +   │  │ specific device  │  │  │
│  │  │ auth code    │  │ OTP (SMS/    │  │ ID. Using token  │  │  │
│  │  │ interception │  │ TOTP) or     │  │ from another     │  │  │
│  │  │ on mobile    │  │ Biometric    │  │ device = blocked │  │  │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │  │
│  │                                                              │  │
│  │  Session Rules:                                              │  │
│  │  • Access token expires in 15 minutes                        │  │
│  │  • Session timeout: 5 min idle for banking apps              │  │
│  │  • Max 3 active devices per user                             │  │
│  │  • Force logout on password change                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  LAYER 3: DATA PROTECTION (At Rest & In Transit)            │  │
│  │                                                              │  │
│  │  ON DEVICE:                          ON SERVER:              │  │
│  │  • EncryptedSharedPrefs              • AES-256 disk encrypt │  │
│  │    (AES-256 via Keystore)            • Column-level encrypt │  │
│  │  • Room DB: Encrypted via             for PII (SSN, DOB)   │  │
│  │    SQLCipher (if needed)             • TDE (Transparent     │  │
│  │  • No sensitive data in logs!          Data Encryption)     │  │
│  │  • ProGuard/R8 obfuscation           • Encryption keys in  │  │
│  │  • No PII in crash reports             AWS KMS / Vault      │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  LAYER 4: APPLICATION SECURITY                              │  │
│  │                                                              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │  │
│  │  │ Input        │  │ Root/        │  │ Screenshot       │  │  │
│  │  │ Validation   │  │ Jailbreak    │  │ Prevention       │  │  │
│  │  │              │  │ Detection    │  │                  │  │  │
│  │  │ Server-side  │  │              │  │ FLAG_SECURE on   │  │  │
│  │  │ ALWAYS.      │  │ Block app on │  │ sensitive screens│  │  │
│  │  │ Never trust  │  │ rooted devs  │  │ (balance, cards) │  │  │
│  │  │ client input │  │ (security    │  │                  │  │  │
│  │  │              │  │ compromised) │  │ Prevents screen  │  │  │
│  │  └──────────────┘  └──────────────┘  │ recording too    │  │  │
│  │                                       └──────────────────┘  │  │
│  │  Also: Rate limiting, session timeout, tamper detection,    │  │
│  │        app integrity checks (Play Integrity API)            │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  LAYER 5: MONITORING & THREAT DETECTION                     │  │
│  │                                                              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │  │
│  │  │ Audit        │  │ Anomaly      │  │ SIEM             │  │  │
│  │  │ Logging      │  │ Detection    │  │ Integration      │  │  │
│  │  │              │  │              │  │                  │  │  │
│  │  │ Every action │  │ ML-based:    │  │ Security Info &  │  │  │
│  │  │ logged with  │  │ • Unusual    │  │ Event Management │  │  │
│  │  │ who, what,   │  │   login loc  │  │                  │  │  │
│  │  │ when, where  │  │ • Large txn  │  │ Correlate events │  │  │
│  │  │              │  │   at 3 AM    │  │ across all       │  │  │
│  │  │ Immutable    │  │ • Rapid      │  │ systems to       │  │  │
│  │  │ (append-only)│  │   succession │  │ detect complex   │  │  │
│  │  │              │  │   transfers  │  │ attack patterns  │  │  │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────┘
```

### 8.2 Authentication Flow — Complete Picture

```
┌──────────────────────────────────────────────────────────────────────┐
│                   COMPLETE AUTH FLOW (Banking App)                    │
│                                                                       │
│  ┌──────────┐                                                        │
│  │  User    │                                                        │
│  │  Opens   │                                                        │
│  │  App     │                                                        │
│  └────┬─────┘                                                        │
│       │                                                              │
│       ▼                                                              │
│  ┌──────────────┐     No      ┌──────────────┐                      │
│  │ Has stored   │────────────→│  Show Login  │                      │
│  │ refresh      │             │  Screen      │                      │
│  │ token?       │             └──────┬───────┘                      │
│  └──────┬───────┘                    │                               │
│         │ Yes                        │ Email + Password              │
│         ▼                            ▼                               │
│  ┌──────────────┐             ┌──────────────┐                      │
│  │ Validate     │             │  POST /auth  │                      │
│  │ refresh      │             │  /login      │                      │
│  │ token with   │             └──────┬───────┘                      │
│  │ server       │                    │                               │
│  └──────┬───────┘             ┌──────┴───────┐                      │
│    Valid│    │Invalid          │  Server says │                      │
│         │    │                 │  "Need OTP"  │                      │
│         │    └──→ Login Screen │              │                      │
│         │                      └──────┬───────┘                      │
│         │                             │                               │
│         │                      ┌──────▼───────┐                      │
│         │                      │  Enter OTP   │                      │
│         │                      │  (or use     │                      │
│         │                      │  Biometric)  │                      │
│         │                      └──────┬───────┘                      │
│         │                             │                               │
│         │                      ┌──────▼───────┐                      │
│         │                      │  POST /auth  │                      │
│         │                      │  /otp/verify │                      │
│         │                      └──────┬───────┘                      │
│         │                             │                               │
│         │    ┌────────────────────────┘                              │
│         │    │  Server returns:                                      │
│         │    │  { access_token, refresh_token, user_profile }       │
│         │    │                                                       │
│         ▼    ▼                                                       │
│  ┌──────────────┐                                                    │
│  │ Store tokens │ → EncryptedSharedPreferences                      │
│  │ Store device │ → Register device with server                     │
│  │ Navigate to  │ → Dashboard / Home screen                         │
│  │ Home         │                                                    │
│  └──────────────┘                                                    │
│                                                                       │
│  SESSION LIFECYCLE:                                                  │
│  • Active: User interacting with app                                 │
│  • Idle timeout: 5 min no interaction → show re-auth prompt          │
│  • Background: App goes to background → start idle timer             │
│  • Expired: refresh_token expired → force full re-login              │
│  • Revoked: Password changed or admin action → force logout          │
└──────────────────────────────────────────────────────────────────────┘
```

### 8.3 Compliance Matrix

```
┌──────────────────────────────────────────────────────────────────────┐
│                  COMPLIANCE BY INDUSTRY                               │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  BANKING / FINANCE                                             │  │
│  │                                                                │  │
│  │  PCI-DSS (Payment Card Industry Data Security Standard)       │  │
│  │  ├── Never store full card numbers on device                  │  │
│  │  ├── Tokenize card data (replace real number with token)      │  │
│  │  ├── Quarterly vulnerability scans by approved vendor         │  │
│  │  └── Annual penetration testing                               │  │
│  │                                                                │  │
│  │  KYC/AML (Know Your Customer / Anti-Money Laundering)         │  │
│  │  ├── Identity verification before account opening             │  │
│  │  ├── Transaction monitoring for suspicious patterns           │  │
│  │  ├── Report suspicious transactions > threshold               │  │
│  │  └── Sanctions screening against watchlists                   │  │
│  │                                                                │  │
│  │  SOX (Sarbanes-Oxley)                                         │  │
│  │  ├── Audit trail for ALL financial data changes               │  │
│  │  ├── Segregation of duties (dev can't deploy to prod)         │  │
│  │  └── Data retention: 7 years minimum                          │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  HEALTHCARE                                                    │  │
│  │                                                                │  │
│  │  HIPAA (Health Insurance Portability and Accountability Act)   │  │
│  │  ├── PHI (Protected Health Info) must be encrypted everywhere │  │
│  │  ├── Role-based access control (nurses ≠ doctors ≠ admin)     │  │
│  │  ├── Audit logs for every PHI access (who viewed what when)   │  │
│  │  ├── BAA (Business Associate Agreement) with cloud provider   │  │
│  │  ├── Breach notification within 60 days                       │  │
│  │  └── Patient right to access their own data                   │  │
│  │                                                                │  │
│  │  HL7/FHIR (Fast Healthcare Interoperability Resources)        │  │
│  │  ├── Standard JSON format for health records                  │  │
│  │  ├── Enables data exchange between hospitals/systems          │  │
│  │  └── RESTful API standard for health data                     │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  ALL INDUSTRIES                                                │  │
│  │                                                                │  │
│  │  GDPR (General Data Protection Regulation — EU)               │  │
│  │  ├── Right to erasure: "Delete all my data"                   │  │
│  │  ├── Data portability: "Export my data in machine-readable"   │  │
│  │  ├── Consent management: Explicit opt-in required             │  │
│  │  ├── Data minimization: Collect only what's needed            │  │
│  │  └── Privacy by design: Security built-in, not bolt-on       │  │
│  │                                                                │  │
│  │  SOC 2 (Service Organization Control 2)                       │  │
│  │  ├── Annual third-party audit of security controls            │  │
│  │  ├── Five principles: Security, Availability,                 │  │
│  │  │   Confidentiality, Processing Integrity, Privacy           │  │
│  │  └── Required by enterprise customers before procurement      │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 9: TESTING STRATEGY

> **Goal:** Ensure the app works correctly, securely, and performs well.
>
> In enterprise, testing isn't optional — it's a gated process. Code cannot
> merge without passing automated tests, and releases cannot happen without
> QA sign-off. A bug in a banking app that incorrectly transfers money or
> a healthcare app that shows the wrong patient record can have severe consequences.

### 9.1 Testing Pyramid

The pyramid shows how many tests of each type you should have. More at the
bottom (fast, cheap), fewer at the top (slow, expensive):

```
                         ╱╲
                        ╱  ╲
                       ╱ E2E╲          ~5% of tests
                      ╱Tests ╲         Slow (minutes), expensive
                     ╱────────╲        Real device/emulator
                    ╱          ╲       Tools: Maestro, Appium
                   ╱ Integration╲      ~15% of tests
                  ╱    Tests     ╲     Medium speed (seconds)
                 ╱────────────────╲    Multiple components together
                ╱                  ╲   Tools: Hilt testing, MockWebServer
               ╱    UI Tests        ╲  ~20% of tests
              ╱  (Compose Testing)    ╲ Compose test rules
             ╱─────────────────────────╲ Tools: ComposeTestRule
            ╱                           ╲
           ╱        Unit Tests            ╲  ~60% of tests
          ╱   (ViewModel, UseCase, Repo)    ╲ Fast (milliseconds)
         ╱   Tools: JUnit5, MockK, Turbine   ╲ No Android needed
        ╱──────────────────────────────────────╲

  WHY this shape?
  • Unit tests run in <1 second each → run thousands quickly
  • E2E tests take 1-5 minutes each → can only run a few
  • A bug caught by a unit test costs 5 min to fix
  • A bug caught in production costs days + reputation damage
```

### 9.2 What to Test at Each Layer

```
┌──────────────┬───────────────────────────────────────────────────────┐
│   Layer      │  What to Test                        │ Tool          │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ ViewModel    │ • State transitions                  │ JUnit5 +      │
│              │   (Loading → Success → Error)        │ Turbine +     │
│              │ • User event handling                │ MockK         │
│              │ • Data transformation/formatting     │               │
│              │ • Edge cases (empty, null, overflow)  │               │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ UseCase      │ • Business rules                     │ JUnit5 +      │
│              │ • Validation (transfer limits, etc.) │ MockK         │
│              │ • Data aggregation from multiple     │               │
│              │   repositories                       │               │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ Repository   │ • Cache-first strategy works         │ JUnit5 +      │
│              │ • API error → fallback to cache      │ MockWebServer │
│              │ • Offline behavior                   │ + In-Memory   │
│              │ • Data mapping (DTO → Domain model) │ Room DB       │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ Room DAO     │ • Queries return correct data        │ Instrumented  │
│              │ • Migrations don't lose data         │ test +        │
│              │ • Edge: empty DB, large datasets     │ Room testing  │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ API / Network│ • JSON parsing (happy + error)       │ MockWebServer │
│              │ • Auth token attached                │               │
│              │ • Error codes handled correctly       │               │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ Compose UI   │ • Correct content displayed          │ ComposeTest   │
│              │ • Loading/error/empty states          │ Rule +        │
│              │ • Click handlers fire correctly       │ Semantics     │
│              │ • Accessibility (content descriptions)│               │
├──────────────┼──────────────────────────────────────┼───────────────┤
│ E2E / Flows  │ • Login → Dashboard → Transfer      │ Maestro /     │
│              │   → OTP → Success (critical path)    │ Appium        │
│              │ • Only happy paths + top 3 error     │               │
│              │   paths                              │               │
└──────────────┴──────────────────────────────────────┴───────────────┘
```

### 9.3 Testing Environments

Each environment serves a different purpose. Code flows left to right,
and issues are caught as early as possible:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│     DEV      │    │      QA      │    │   STAGING    │    │     PROD     │
│              │    │              │    │              │    │              │
│ ┌──────────┐ │    │ ┌──────────┐ │    │ ┌──────────┐ │    │ ┌──────────┐ │
│ │Mock APIs │ │───→│ │Test APIs │ │───→│ │Prod-clone│ │───→│ │Real APIs │ │
│ │Mock Data │ │    │ │Test Data │ │    │ │Sanitized │ │    │ │Real Data │ │
│ │Local DB  │ │    │ │Test DB   │ │    │ │data      │ │    │ │Prod DB   │ │
│ └──────────┘ │    │ └──────────┘ │    │ └──────────┘ │    │ └──────────┘ │
│              │    │              │    │              │    │              │
│ WHO:         │    │ WHO:         │    │ WHO:         │    │ WHO:         │
│ Developers   │    │ QA team      │    │ UAT testers  │    │ Real users   │
│              │    │              │    │ + Perf team   │    │              │
│ TESTS:       │    │ TESTS:       │    │ TESTS:       │    │ MONITORS:    │
│ Unit tests   │    │ Manual + auto│    │ Load testing │    │ Crashlytics  │
│ Local runs   │    │ Regression   │    │ Security pen │    │ Analytics    │
│              │    │ Exploratory  │    │ test         │    │ Error rates  │
│              │    │              │    │ Compliance   │    │ App reviews  │
│              │    │              │    │ audit        │    │              │
│ GATE:        │    │ GATE:        │    │ GATE:        │    │              │
│ PR approved  │    │ QA sign-off  │    │ All audits   │    │              │
│ Tests pass   │    │ No P1 bugs   │    │ passed       │    │              │
│ Code review  │    │              │    │ PO approval  │    │              │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
```

---

## PHASE 10: CI/CD PIPELINE

> **Goal:** Automate build, test, and deployment so humans don't have to do it manually.
>
> CI (Continuous Integration) = Automatically build and test every code change
> CD (Continuous Delivery) = Automatically deliver tested builds to QA/production
>
> Without CI/CD, a developer would have to: pull code, build locally, run tests,
> manually upload APK to Play Store, send it to QA via email — and all of this
> is error-prone. CI/CD does it all automatically on every Git push.

### 10.1 Pipeline Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                       CI/CD PIPELINE                                  │
│                                                                       │
│  Developer pushes to feature branch (Git)                            │
│       │                                                               │
│       │  Trigger: Push to any branch / PR created / PR merged        │
│       ▼                                                               │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  STAGE 1: BUILD & STATIC ANALYSIS (2-3 min)                   │  │
│  │                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │  • Compile Kotlin code (detect compile errors)           │ │  │
│  │  │  • Run Android Lint (find potential bugs, performance)   │ │  │
│  │  │  • Run Detekt (static analysis: complexity, code smells) │ │  │
│  │  │  • Run ktlint (formatting: consistent code style)        │ │  │
│  │  │  • Check Gradle dependency locks (no unexpected changes) │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  │  ❌ Fail? → Block PR merge. Dev fixes and pushes again.      │  │
│  └──────────────────────────┬─────────────────────────────────────┘  │
│                             │ ✅ Pass                                │
│                             ▼                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  STAGE 2: UNIT TESTS + COVERAGE (5-10 min)                    │  │
│  │                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │  • Run all unit tests (JUnit5 + MockK + Turbine)         │ │  │
│  │  │  • Generate code coverage report (Kover / JaCoCo)        │ │  │
│  │  │  • Enforce minimum coverage: 70% overall, 80% for core   │ │  │
│  │  │  • Publish coverage report to PR as comment               │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  │  ❌ Fail? → Tests broken or coverage dropped below threshold  │  │
│  └──────────────────────────┬─────────────────────────────────────┘  │
│                             │ ✅ Pass                                │
│                             ▼                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  STAGE 3: SECURITY SCAN (3-5 min)                             │  │
│  │                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │  • Dependency vulnerability scan (OWASP Dependency-Check)│ │  │
│  │  │  • Secret detection (no API keys, passwords in code)     │ │  │
│  │  │  • License compliance (no GPL in proprietary app)        │ │  │
│  │  │  • SAST: Static Application Security Testing             │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  │  ❌ Fail? → Critical vulnerability found. Must fix or waive. │  │
│  └──────────────────────────┬─────────────────────────────────────┘  │
│                             │ ✅ Pass                                │
│                             ▼                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  STAGE 4: BUILD ARTIFACTS (5-8 min)                           │  │
│  │                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │  • Build debug APK (for QA/testers)                      │ │  │
│  │  │  • Build release AAB (signed with upload key)            │ │  │
│  │  │  • ProGuard/R8 obfuscation + optimization                │ │  │
│  │  │  • Generate mapping.txt (for crash report deobfuscation) │ │  │
│  │  │  • Upload mapping to Crashlytics                         │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────┬─────────────────────────────────────┘  │
│                             │ ✅ Pass                                │
│                             ▼                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  STAGE 5: DISTRIBUTE (2-3 min)                                │  │
│  │                                                                │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │  Feature branch:                                         │ │  │
│  │  │    → Upload debug APK to Firebase App Distribution       │ │  │
│  │  │    → Notify QA on Slack with download link               │ │  │
│  │  │                                                          │ │  │
│  │  │  Develop branch (after merge):                           │ │  │
│  │  │    → Upload to Play Store Internal Testing track         │ │  │
│  │  │                                                          │ │  │
│  │  │  Release branch / tag:                                   │ │  │
│  │  │    → Upload to Play Store Production (staged rollout)    │ │  │
│  │  │    → Create GitHub Release with changelog                │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  Pipeline Tools:                                                      │
│  • GitHub Actions (most popular for Android)                         │
│  • Bitrise (Android-specific CI, simpler setup)                      │
│  • GitLab CI, Jenkins, CircleCI (alternatives)                       │
│  • Fastlane (automates signing, uploading, changelogs)               │
└──────────────────────────────────────────────────────────────────────┘
```

### 10.2 Release Tracks (Google Play)

Apps go through multiple testing phases before reaching real users:

```
┌──────────────────────────────────────────────────────────────────────┐
│               GOOGLE PLAY RELEASE TRACKS                             │
│                                                                       │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐             │
│  │   Internal   │   │    Closed    │   │     Open     │             │
│  │   Testing    │──→│     Beta     │──→│     Beta     │             │
│  │              │   │              │   │              │             │
│  │ ~10-20 people│   │ ~100-500     │   │ ~1000-5000   │             │
│  │ Dev team + QA│   │ QA + trusted │   │ Opt-in users │             │
│  │              │   │ testers      │   │ from public  │             │
│  │ Purpose:     │   │              │   │              │             │
│  │ Smoke test,  │   │ Purpose:     │   │ Purpose:     │             │
│  │ basic sanity │   │ Full QA      │   │ Real-world   │             │
│  │              │   │ regression   │   │ validation   │             │
│  └──────────────┘   └──────────────┘   └──────┬───────┘             │
│                                                │                     │
│                                                ▼                     │
│                                    ┌────────────────────┐           │
│                                    │   PRODUCTION        │           │
│                                    │                     │           │
│                                    │  Staged Rollout:    │           │
│                                    │                     │           │
│                                    │  Day 1:    1%  ───┐ │           │
│                                    │  Day 2:    5%     │ │           │
│                                    │  Day 3:   20%     │ Monitor    │
│                                    │  Day 5:   50%     │ crash rate │
│                                    │  Day 7:  100% ◄──┘ │           │
│                                    │                     │           │
│                                    │  At any point:      │           │
│                                    │  crash rate > 0.5%  │           │
│                                    │  → HALT rollout     │           │
│                                    │  → Investigate      │           │
│                                    │  → Hotfix or revert │           │
│                                    └────────────────────┘           │
└──────────────────────────────────────────────────────────────────────┘
```

---

## PHASE 11: DEPLOYMENT & RELEASE

> **Goal:** Ship the app to production safely and be ready to roll back if things go wrong.
>
> Enterprise deployment is NOT "push to Play Store and pray." It involves
> infrastructure provisioning, environment configuration, database migrations,
> feature flag setup, and a detailed rollback plan. In banking, a bad deployment
> can mean users can't access their money — so the process is highly controlled.

### 11.1 Infrastructure (Cloud — AWS Example)

This is the physical/virtual infrastructure running the backend. The Android
app talks to this through the API Gateway:

```
┌──────────────────────────────────────────────────────────────────────┐
│                    AWS CLOUD INFRASTRUCTURE                          │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    VPC (Virtual Private Cloud)                  │  │
│  │     Your own isolated network in AWS. Nothing gets in/out      │  │
│  │     unless explicitly allowed.                                 │  │
│  │                                                                │  │
│  │  ┌────── Public Subnet ──────┐  ┌──── Private Subnet ──────┐ │  │
│  │  │  (Internet-facing)        │  │  (No direct internet)     │ │  │
│  │  │                           │  │                           │ │  │
│  │  │  ┌───────────────────┐    │  │  ┌──────────────────┐    │ │  │
│  │  │  │  ALB (Application │    │  │  │  ECS / EKS       │    │ │  │
│  │  │  │  Load Balancer)   │────┤──┤─→│  (Containers)    │    │ │  │
│  │  │  │                   │    │  │  │                   │    │ │  │
│  │  │  │  Distributes      │    │  │  │  Auth   ×3 pods  │    │ │  │
│  │  │  │  traffic across   │    │  │  │  Core   ×5 pods  │    │ │  │
│  │  │  │  healthy pods     │    │  │  │  Txn    ×5 pods  │    │ │  │
│  │  │  └───────────────────┘    │  │  │  Cards  ×3 pods  │    │ │  │
│  │  │                           │  │  │  Notif  ×2 pods  │    │ │  │
│  │  │  ┌───────────────────┐    │  │  └──────────────────┘    │ │  │
│  │  │  │  WAF + API        │    │  │                           │ │  │
│  │  │  │  Gateway          │    │  │  ┌──────────────────┐    │ │  │
│  │  │  │  (First line of   │    │  │  │  RDS PostgreSQL  │    │ │  │
│  │  │  │   defense)        │    │  │  │  (Multi-AZ: auto │    │ │  │
│  │  │  └───────────────────┘    │  │  │   failover if    │    │ │  │
│  │  │                           │  │  │   one AZ goes    │    │ │  │
│  │  │  ┌───────────────────┐    │  │  │   down)          │    │ │  │
│  │  │  │  CloudFront (CDN) │    │  │  └──────────────────┘    │ │  │
│  │  │  │  Static assets    │    │  │                           │ │  │
│  │  │  │  cached globally  │    │  │  ┌──────────────────┐    │ │  │
│  │  │  └───────────────────┘    │  │  │  ElastiCache     │    │ │  │
│  │  │                           │  │  │  (Redis cluster) │    │ │  │
│  │  └───────────────────────────┘  │  └──────────────────┘    │ │  │
│  │                                  │                          │ │  │
│  │                                  │  ┌──────────────────┐   │ │  │
│  │                                  │  │  MSK (Managed    │   │ │  │
│  │                                  │  │  Kafka)          │   │ │  │
│  │                                  │  └──────────────────┘   │ │  │
│  │                                  └──────────────────────────┘ │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  Supporting Services:                                                │
│  ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌────────────┐ ┌────────┐ │
│  │    S3    │ │   SES    │ │CloudWatch │ │  Secrets   │ │  KMS   │ │
│  │ (Files, │ │ (Email   │ │(Logs,     │ │  Manager   │ │(Encryp-│ │
│  │  backups)│ │  alerts) │ │ metrics,  │ │(API keys,  │ │ tion   │ │
│  │         │ │          │ │ alarms)   │ │ DB creds)  │ │ keys)  │ │
│  └──────────┘ └──────────┘ └───────────┘ └────────────┘ └────────┘ │
│                                                                       │
│  Infrastructure as Code: Terraform / AWS CDK                         │
│  (Every piece of infra is defined in code, version-controlled,      │
│   and reproducible. No manual clicking in AWS console.)             │
└──────────────────────────────────────────────────────────────────────┘
```

### 11.2 Deployment Strategies

```
┌──────────────────────────────────────────────────────────────────────┐
│              DEPLOYMENT STRATEGIES COMPARED                          │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  1. ROLLING DEPLOYMENT (most common)                          │  │
│  │                                                                │  │
│  │  Old: [v1] [v1] [v1] [v1] [v1]                               │  │
│  │       [v2] [v1] [v1] [v1] [v1]  ← Replace one at a time     │  │
│  │       [v2] [v2] [v1] [v1] [v1]                               │  │
│  │       [v2] [v2] [v2] [v2] [v2]  ← All updated               │  │
│  │                                                                │  │
│  │  Pro: Zero downtime, gradual                                  │  │
│  │  Con: Brief period with mixed versions serving traffic        │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  2. BLUE-GREEN DEPLOYMENT (enterprise banking)                │  │
│  │                                                                │  │
│  │  Blue (current):  [v1] [v1] [v1]  ← Serving traffic          │  │
│  │  Green (new):     [v2] [v2] [v2]  ← Ready, tested            │  │
│  │                                                                │  │
│  │  Switch: Load balancer points to Green                        │  │
│  │  Rollback: Point back to Blue (instant!)                      │  │
│  │                                                                │  │
│  │  Pro: Instant rollback, zero downtime                         │  │
│  │  Con: Requires 2x infrastructure (costly)                     │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  3. CANARY DEPLOYMENT (for risky changes)                     │  │
│  │                                                                │  │
│  │  Main:   [v1] [v1] [v1] [v1] [v1]  ← 95% traffic            │  │
│  │  Canary: [v2]                        ← 5% traffic             │  │
│  │                                                                │  │
│  │  Monitor canary: error rate, latency, crashes                 │  │
│  │  If OK → gradually shift traffic to v2                        │  │
│  │  If BAD → kill canary, all traffic stays on v1                │  │
│  │                                                                │  │
│  │  Pro: Real user validation with minimal risk                  │  │
│  │  Con: More complex routing and monitoring setup               │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

### 11.3 Release Checklist

```
┌──────────────────────────────────────────────────────────────────┐
│                    RELEASE CHECKLIST                              │
│                                                                   │
│  PRE-RELEASE (before release day):                               │
│  ────────────────────────────────────────────                    │
│  □ All CI pipeline stages pass (build, test, security)           │
│  □ QA regression testing complete — no P1/P2 bugs open           │
│  □ Performance benchmarks met:                                    │
│     • API response: <200ms (p95)                                 │
│     • App cold start: <2 seconds                                 │
│     • APK size: <50MB                                            │
│  □ Accessibility audit passed (TalkBack, contrast, touch size)   │
│  □ Legal/compliance review signed off                            │
│  □ Release notes written (user-facing + internal)                │
│  □ Feature flags configured (new features behind flags)          │
│  □ Database migration tested on staging                          │
│  □ Rollback plan documented and tested                           │
│  □ On-call schedule confirmed for release window                 │
│                                                                   │
│  RELEASE DAY:                                                     │
│  ────────────────────────────────────────────                    │
│  □ Tag release in Git (v2.5.0)                                   │
│  □ Build signed AAB from tagged commit (not from local machine!) │
│  □ Deploy backend changes first (backward compatible)            │
│  □ Upload AAB to Play Store → start staged rollout (1-5%)       │
│  □ Upload mapping.txt to Crashlytics                             │
│  □ Monitor real-time: Crashlytics, CloudWatch, Grafana           │
│  □ Team on standby in Slack #release channel                     │
│                                                                   │
│  POST-RELEASE (3-7 days):                                        │
│  ────────────────────────────────────────────                    │
│  □ Staged rollout progression: 1% → 5% → 25% → 50% → 100%     │
│  □ Halt criteria: crash-free rate drops below 99.5%              │
│  □ Monitor Play Store reviews for new complaints                 │
│  □ Verify analytics events firing correctly                      │
│  □ Update internal documentation                                 │
│  □ Sprint retrospective includes release review                  │
│  □ Close JIRA tickets for shipped features                       │
└──────────────────────────────────────────────────────────────────┘

### 11.2 Deployment Strategies

```
┌──────────────────────────────────────────────────────────────────────┐
│              DEPLOYMENT STRATEGIES COMPARED                          │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  1. ROLLING DEPLOYMENT (most common)                          │  │
│  │                                                                │  │
│  │  Old: [v1] [v1] [v1] [v1] [v1]                               │  │
│  │       [v2] [v1] [v1] [v1] [v1]  ← Replace one at a time     │  │
│  │       [v2] [v2] [v1] [v1] [v1]                               │  │
│  │       [v2] [v2] [v2] [v2] [v2]  ← All updated               │  │
│  │                                                                │  │
│  │  Pro: Zero downtime, gradual                                  │  │
│  │  Con: Brief period with mixed versions serving traffic        │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  2. BLUE-GREEN DEPLOYMENT (enterprise banking)                │  │
│  │                                                                │  │
│  │  Blue (current):  [v1] [v1] [v1]  ← Serving traffic          │  │
│  │  Green (new):     [v2] [v2] [v2]  ← Ready, tested            │  │
│  │                                                                │  │
│  │  Switch: Load balancer points to Green                        │  │
│  │  Rollback: Point back to Blue (instant!)                      │  │
│  │                                                                │  │
│  │  Pro: Instant rollback, zero downtime                         │  │
│  │  Con: Requires 2x infrastructure (costly)                     │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  3. CANARY DEPLOYMENT (for risky changes)                     │  │
│  │                                                                │  │
│  │  Main:   [v1] [v1] [v1] [v1] [v1]  ← 95% traffic            │  │
│  │  Canary: [v2]                        ← 5% traffic             │  │
│  │                                                                │  │
│  │  Monitor canary: error rate, latency, crashes                 │  │
│  │  If OK → gradually shift traffic to v2                        │  │
│  │  If BAD → kill canary, all traffic stays on v1                │  │
│  │                                                                │  │
│  │  Pro: Real user validation with minimal risk                  │  │
│  │  Con: More complex routing and monitoring setup               │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

### 11.3 Release Checklist

```
┌──────────────────────────────────────────────────────────────────┐
│                    RELEASE CHECKLIST                              │
│                                                                   │
│  PRE-RELEASE (before release day):                               │
│  ────────────────────────────────────────────                    │
│  □ All CI pipeline stages pass (build, test, security)           │
│  □ QA regression testing complete — no P1/P2 bugs open           │
│  □ Performance benchmarks met:                                    │
│     • API response: <200ms (p95)                                 │
│     • App cold start: <2 seconds                                 │
│     • APK size: <50MB                                            │
│  □ Accessibility audit passed (TalkBack, contrast, touch size)   │
│  □ Legal/compliance review signed off                            │
│  □ Release notes written (user-facing + internal)                │
│  □ Feature flags configured (new features behind flags)          │
│  □ Database migration tested on staging                          │
│  □ Rollback plan documented and tested                           │
│  □ On-call schedule confirmed for release window                 │
│                                                                   │
│  RELEASE DAY:                                                     │
│  ────────────────────────────────────────────                    │
│  □ Tag release in Git (v2.5.0)                                   │
│  □ Build signed AAB from tagged commit (not from local machine!) │
│  □ Deploy backend changes first (backward compatible)            │
│  □ Upload AAB to Play Store → start staged rollout (1-5%)       │
│  □ Upload mapping.txt to Crashlytics                             │
│  □ Monitor real-time: Crashlytics, CloudWatch, Grafana           │
│  □ Team on standby in Slack #release channel                     │
│                                                                   │
│  POST-RELEASE (3-7 days):                                        │
│  ────────────────────────────────────────────                    │
│  □ Staged rollout progression: 1% → 5% → 25% → 50% → 100%     │
│  □ Halt criteria: crash-free rate drops below 99.5%              │
│  □ Monitor Play Store reviews for new complaints                 │
│  □ Verify analytics events firing correctly                      │
│  □ Update internal documentation                                 │
│  □ Sprint retrospective includes release review                  │
│  □ Close JIRA tickets for shipped features                       │
└──────────────────────────────────────────────────────────────────┘
```

---

## PHASE 12: POST-LAUNCH OPERATIONS

> **Goal:** Keep the app healthy, detect problems before users do, and iterate.
>
> Launching is NOT the finish line — it's the starting line. Enterprise apps
> are living products that need constant monitoring, incident response, and
> continuous improvement. The operations team monitors the app 24/7, and
> developers rotate on-call duty to fix production issues quickly.

### 12.1 Monitoring & Observability (The Three Pillars)

Observability answers: "Is the system healthy?" and "When something breaks, WHY?"

```
┌──────────────────────────────────────────────────────────────────────┐
│            THE THREE PILLARS OF OBSERVABILITY                        │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  PILLAR 1: METRICS — "How is the system performing?"           │ │
│  │                                                                 │ │
│  │  ┌─────── ANDROID APP ───────┐  ┌─────── BACKEND ────────────┐│ │
│  │  │                           │  │                             ││ │
│  │  │ Firebase Crashlytics:     │  │ Prometheus + Grafana:       ││ │
│  │  │  • Crash-free rate >99.9% │  │  • API latency (p50/p99)   ││ │
│  │  │  • ANR rate <0.47%        │  │  • Request rate (req/sec)  ││ │
│  │  │                           │  │  • Error rate (% of 5xx)   ││ │
│  │  │ Firebase Performance:     │  │  • CPU/memory utilization  ││ │
│  │  │  • Cold start time <2s    │  │  • DB query latency        ││ │
│  │  │  • Network latency        │  │  • Queue depth (Kafka lag) ││ │
│  │  │  • Screen render time     │  │                             ││ │
│  │  │                           │  │ Custom Business Metrics:    ││ │
│  │  │ Play Console Vitals:      │  │  • Transactions/minute     ││ │
│  │  │  • Startup time           │  │  • Login success rate      ││ │
│  │  │  • Battery usage          │  │  • Transfer completion rate││ │
│  │  │  • Permission denials     │  │                             ││ │
│  │  └───────────────────────────┘  └─────────────────────────────┘│ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  PILLAR 2: LOGS — "What happened?"                             │ │
│  │                                                                 │ │
│  │  ELK Stack (Elasticsearch + Logstash + Kibana):                │ │
│  │  • Centralized logs from ALL microservices                      │ │
│  │  • Structured logging (JSON format)                             │ │
│  │  • Searchable: "Show all failed transfers in last hour"        │ │
│  │  • Correlation ID links logs across services for one request   │ │
│  │                                                                 │ │
│  │  Android-side: Firebase Crashlytics custom logs                │ │
│  │  Rule: NEVER log PII (names, emails, account numbers)         │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  PILLAR 3: TRACES — "Where is the bottleneck?"                 │ │
│  │                                                                 │ │
│  │  Jaeger / Zipkin / AWS X-Ray:                                  │ │
│  │  • Distributed tracing across microservices                     │ │
│  │  • Shows the full journey of ONE request:                      │ │
│  │                                                                 │ │
│  │    App ─[50ms]─→ Gateway ─[5ms]─→ Transfer Svc ─[20ms]─→     │ │
│  │    Account Svc ─[150ms]─→ DB Query ─[8ms]─→ Kafka Publish     │ │
│  │                                                                 │ │
│  │    Total: 233ms — Bottleneck: Account Service (150ms!)         │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                       │
│  ALERTING:                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  PagerDuty / OpsGenie:                                         │ │
│  │                                                                 │ │
│  │  Alert Rules:                                                   │ │
│  │  • Crash-free rate < 99.5%  → Page on-call (SEV 2)            │ │
│  │  • API error rate > 1%      → Page on-call (SEV 2)            │ │
│  │  • API p99 latency > 2s     → Slack alert (SEV 3)             │ │
│  │  • Database CPU > 80%       → Slack alert (SEV 3)             │ │
│  │  • Kafka consumer lag > 10K → Page on-call (SEV 2)            │ │
│  │  • Zero transactions for 5m → Page EVERYONE (SEV 1!)          │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

### 12.2 Feature Flags — Decouple Deploy from Release

Feature flags let you deploy code to production but control WHO sees it.
This means you can deploy on Monday, test with internal users, and "release"
to real users on Thursday — without a new deployment:

```
┌──────────────────────────────────────────────────────────────────────┐
│                   FEATURE FLAG LIFECYCLE                              │
│                                                                       │
│  Tool: Firebase Remote Config / LaunchDarkly / Unleash               │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  Step 1: Deploy code with feature behind a flag               │  │
│  │                                                                │  │
│  │  if (featureFlags.isEnabled("new_transfer_ui")) {             │  │
│  │      NewTransferScreen()   // New Compose screen              │  │
│  │  } else {                                                      │  │
│  │      OldTransferScreen()   // Current stable screen           │  │
│  │  }                                                             │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  Step 2: Gradual rollout via dashboard (no app update!)       │  │
│  │                                                                │  │
│  │  Week 1:  Internal team only (10 people)                      │  │
│  │           ████░░░░░░░░░░░░░░░░░░  1%                         │  │
│  │                                                                │  │
│  │  Week 2:  Beta testers (500 people)                           │  │
│  │           █████████░░░░░░░░░░░░░  10%                        │  │
│  │                                                                │  │
│  │  Week 3:  Wider audience                                      │  │
│  │           ██████████████░░░░░░░░  50%                        │  │
│  │                                                                │  │
│  │  Week 4:  Everyone                                            │  │
│  │           ████████████████████░░  100%                       │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  KILL SWITCH: If issues detected at any point:                │  │
│  │                                                                │  │
│  │  Set flag to 0% → instantly rolls back to old UI              │  │
│  │  No app update needed. No Play Store review. Instant.         │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

### 12.3 Incident Response

When something goes wrong in production, the team follows a structured
incident response process:

```
┌──────────────────────────────────────────────────────────────────────┐
│                INCIDENT SEVERITY LEVELS                               │
├─────────┬─────────────────────────┬──────────────────────────────────┤
│  SEV    │  Definition             │  Response                        │
├─────────┼─────────────────────────┼──────────────────────────────────┤
│ SEV 1   │ App completely down,    │ All hands on deck. 15 min ack.  │
│ CRITICAL│ data breach, money lost │ War room. CTO + CEO notified.   │
│         │                         │ Status page updated.             │
├─────────┼─────────────────────────┼──────────────────────────────────┤
│ SEV 2   │ Major feature broken    │ On-call team. 30 min ack.       │
│ HIGH    │ (payments, login fail)  │ Engineering manager notified.    │
│         │ >10% users affected     │ Hotfix branch created.           │
├─────────┼─────────────────────────┼──────────────────────────────────┤
│ SEV 3   │ Minor feature issue,    │ Next business day response.      │
│ MEDIUM  │ workaround available    │ Bug ticket in JIRA.              │
│         │ <5% users affected      │                                  │
├─────────┼─────────────────────────┼──────────────────────────────────┤
│ SEV 4   │ Cosmetic issue, typo,   │ Prioritized in next sprint.      │
│ LOW     │ minor UX imperfection   │                                  │
└─────────┴─────────────────────────┴──────────────────────────────────┘

INCIDENT TIMELINE (SEV 1 Example):

  ┌─────────┬──────────────────────────────────────────────────────┐
  │  Time   │  Action                                              │
  ├─────────┼──────────────────────────────────────────────────────┤
  │  T+0    │  Alert fires (PagerDuty pages on-call engineer)     │
  │  T+5m   │  On-call acknowledges, starts investigating         │
  │  T+15m  │  Initial assessment: "Transfer API returning 500s"  │
  │         │  Severity assigned: SEV 1. War room opened.         │
  │  T+20m  │  Status page updated: "Investigating transfer issue"│
  │  T+30m  │  Root cause identified: Bad DB migration in deploy  │
  │  T+45m  │  Fix: Rollback deployment to previous version       │
  │  T+60m  │  Service restored. Monitoring closely.              │
  │  T+2h   │  Confirmed stable. War room closed.                 │
  │  T+24h  │  Post-mortem document written (blameless!)          │
  │  T+48h  │  Post-mortem review meeting with team               │
  │  T+1w   │  Action items from post-mortem completed            │
  └─────────┴──────────────────────────────────────────────────────┘

POST-MORTEM TEMPLATE (Blameless):
  • What happened? (Timeline)
  • What was the impact? (Users affected, duration, financial impact)
  • Root cause? (5 Whys analysis)
  • What went well? (Detection was fast, rollback worked, etc.)
  • What can we improve? (Action items with owners and deadlines)
  • How do we prevent this class of issue? (Systemic fix, not band-aid)
```

---

## END-TO-END TIMELINE (Typical Enterprise App)

> A realistic timeline for an enterprise Android app with a team of 15-20.
> Key insight: many phases run **in parallel**, not sequentially.
> Also: "MVP" in enterprise doesn't mean a half-baked app. It means the
> smallest version that meets compliance, security, and core business needs.

```
┌───────────────────────────────────────────────────────────────────────────────┐
│              ENTERPRISE APP TIMELINE — 8 MONTH MVP                           │
│                                                                               │
│  Phase                  │ M1  │ M2  │ M3  │ M4  │ M5  │ M6  │ M7  │ M8     │
│  ───────────────────────┼─────┼─────┼─────┼─────┼─────┼─────┼─────┼────    │
│  Discovery & Planning   │████ │██   │     │     │     │     │     │        │
│  UI/UX Design           │  ██ │████ │██   │     │     │     │     │        │
│  Architecture Design    │  ██ │████ │     │     │     │     │     │        │
│  Infrastructure Setup   │     │ ████│██   │     │     │     │     │        │
│  Backend Development    │     │   ██│█████│█████│████ │     │     │        │
│  Android Development    │     │     │ ████│█████│█████│████ │     │        │
│  API Integration        │     │     │   ██│█████│████ │     │     │        │
│  Security Implementation│     │     │ ████│████ │████ │████ │     │        │
│  Testing (ongoing)      │     │     │   ██│█████│█████│█████│████ │        │
│  Penetration Testing    │     │     │     │     │     │ ████│██   │        │
│  UAT & Beta             │     │     │     │     │     │   ██│█████│██      │
│  Production Launch      │     │     │     │     │     │     │   ██│████    │
│                         │     │     │     │     │     │     │     │        │
│  ───────── KEY MILESTONES ──────────                                       │
│  M1 end:  Requirements signed off ✓                                       │
│  M2 end:  Architecture approved, Infra ready ✓                            │
│  M3 end:  First API endpoint live, first app screen built ✓               │
│  M4 end:  Core features working end-to-end ✓                              │
│  M5 end:  Feature-complete, internal alpha ✓                              │
│  M6 end:  Security audit passed, performance benchmarks met ✓             │
│  M7 end:  UAT passed, beta deployed to 500+ testers ✓                    │
│  M8 end:  Production launch (staged rollout) ✓                            │
│                                                                               │
│  AFTER LAUNCH:                                                               │
│  Month 9-10:  Bug fixes, stability improvements, metrics review              │
│  Month 10-12: v2.0 features, performance optimization                       │
│  Ongoing:     Bi-weekly releases, on-call rotation, quarterly security audit │
│                                                                               │
│  Total MVP: ~6-8 months (experienced team of 15-20)                          │
│  Total with ramp-up/hiring: ~9-12 months                                     │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

## TEAM STRUCTURE

> Enterprise teams follow a "two-pizza team" principle — each sub-team
> should be small enough that two pizzas can feed them (5-8 people).
> Teams are cross-functional: each has enough skills to build, test,
> and deploy features independently.

```
┌──────────────────────────────────────────────────────────────────────┐
│               ENTERPRISE TEAM STRUCTURE (18-22 people)               │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                  LEADERSHIP (3)                                │  │
│  │                                                                │  │
│  │  Engineering Manager  — Delivery, hiring, team health         │  │
│  │  Product Owner        — Priorities, stakeholders, roadmap     │  │
│  │  Tech Lead / Architect— Technical direction, code quality     │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────── Squad 1 ──────────┐  ┌────────── Squad 2 ──────────┐  │
│  │ "Accounts & Onboarding"     │  │  "Payments & Transfers"     │  │
│  │                              │  │                              │  │
│  │  1 Sr Android dev            │  │  1 Sr Android dev           │  │
│  │  1 Mid Android dev           │  │  1 Sr Android dev           │  │
│  │  1 Sr Backend dev            │  │  1 Sr Backend dev           │  │
│  │  1 Mid Backend dev           │  │  1 Sr Backend dev           │  │
│  │  1 QA engineer               │  │  1 QA engineer              │  │
│  └──────────────────────────────┘  └──────────────────────────────┘  │
│                                                                       │
│  ┌────────── Platform Team ─────────────────────────────────────┐   │
│  │ "Shared infrastructure, tooling, CI/CD" (4)                  │   │
│  │                                                               │   │
│  │  1 DevOps / SRE engineer     — Infra, CI/CD, monitoring      │   │
│  │  1 DevOps / SRE engineer     — On-call, incident response    │   │
│  │  1 Sr Backend dev (platform) — Shared libraries, auth        │   │
│  │  1 Security engineer (AppSec)— Pen testing, code review      │   │
│  └───────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌────────── Design & Analytics ────────────────────────────────┐   │
│  │  1 UX Designer              — Research, wireframes, design   │   │
│  │  1 UI Designer              — Figma, design system           │   │
│  │  1 Business Analyst         — Requirements, acceptance criteria│  │
│  └───────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  COMMUNICATION PATTERNS:                                             │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  Daily:   Squad standups (15 min)                             │  │
│  │  Weekly:  Cross-squad sync (30 min), Sprint planning          │  │
│  │  Biweekly: Sprint demo to stakeholders                        │  │
│  │  Monthly: Architecture review, tech debt prioritization       │  │
│  │  Quarterly: Roadmap planning, security audit                  │  │
│  │                                                                │  │
│  │  Tools: Slack (async), Jira (tracking), Confluence (docs),    │  │
│  │         Figma (design), GitHub (code), Miro (diagrams)        │  │
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## QUICK REFERENCE: TECHNOLOGY MAP

```
╔═══════════════════════════════════════════════════════════════════════════════╗
║              ENTERPRISE ANDROID APP — COMPLETE TECH MAP                      ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  ┌─────────── ANDROID CLIENT ─────────────┐                                 ║
║  │                                         │                                 ║
║  │  Language:     Kotlin (100%)            │                                 ║
║  │  UI:           Jetpack Compose          │                                 ║
║  │  DI:           Hilt (Dagger under hood) │                                 ║
║  │  Networking:   Retrofit + OkHttp        │                                 ║
║  │  Local DB:     Room (SQLite)            │                                 ║
║  │  Async:        Coroutines + Flow        │                                 ║
║  │  Images:       Coil                     │                                 ║
║  │  Preferences:  DataStore (Proto)        │                                 ║
║  │  Navigation:   Navigation Compose       │                                 ║
║  │  Auth:         Biometric API + KeyStore │                                 ║
║  │  Security:     Security Crypto lib      │                                 ║
║  │  Background:   WorkManager              │                                 ║
║  │  Serialization:Kotlinx Serialization    │                                 ║
║  │  Build:        Gradle (KTS) + Version   │                                 ║
║  │                Catalog                   │                                 ║
║  └─────────────────────────────────────────┘                                 ║
║                                                                               ║
║  ┌─────────── BACKEND ────────────────────┐                                  ║
║  │                                         │                                 ║
║  │  Framework:    Spring Boot (Kotlin)     │                                 ║
║  │  Database:     PostgreSQL + Redis       │                                 ║
║  │  Messaging:    Apache Kafka             │                                 ║
║  │  Auth:         Keycloak (OAuth 2.0)     │                                 ║
║  │  Search:       Elasticsearch            │                                 ║
║  │  File Storage: AWS S3                   │                                 ║
║  │  Internal RPC: gRPC (between services) │                                 ║
║  │  API Format:   REST (JSON) for mobile   │                                 ║
║  │  Gateway:      Spring Cloud Gateway     │                                 ║
║  │  Docs:         OpenAPI / Swagger        │                                 ║
║  └─────────────────────────────────────────┘                                 ║
║                                                                               ║
║  ┌─────────── INFRA / DEVOPS ─────────────┐                                 ║
║  │                                         │                                 ║
║  │  Cloud:        AWS (EKS, RDS, S3, etc.) │                                ║
║  │  IaC:          Terraform                │                                 ║
║  │  Containers:   Docker + Kubernetes      │                                 ║
║  │  CI/CD:        GitHub Actions           │                                 ║
║  │  Monitoring:   Prometheus + Grafana     │                                 ║
║  │  Logging:      ELK Stack               │                                 ║
║  │  Tracing:      Jaeger / AWS X-Ray      │                                 ║
║  │  Alerting:     PagerDuty / OpsGenie    │                                 ║
║  │  Feature Flags:Firebase Remote Config   │                                 ║
║  │  Crash Report: Firebase Crashlytics     │                                 ║
║  │  Analytics:    Firebase + Mixpanel      │                                 ║
║  │  Push:         Firebase Cloud Messaging │                                 ║
║  │  CDN:          CloudFront               │                                 ║
║  │  Secrets:      AWS Secrets Manager      │                                 ║
║  └─────────────────────────────────────────┘                                 ║
║                                                                               ║
║  ┌─────────── TESTING ────────────────────┐                                  ║
║  │                                         │                                 ║
║  │  Unit:         JUnit 5 + MockK         │                                 ║
║  │  UI:           Compose Testing          │                                 ║
║  │  Integration:  Testcontainers           │                                 ║
║  │  E2E:          Maestro / Espresso       │                                 ║
║  │  API:          Postman / REST Assured   │                                 ║
║  │  Performance:  JMeter / k6              │                                 ║
║  │  Security:     OWASP ZAP / Burp Suite  │                                 ║
║  │  Coverage:     Jacoco + Kover           │                                 ║
║  └─────────────────────────────────────────┘                                 ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
```

---

## FINAL THOUGHTS

Building an enterprise Android app is a team sport. It requires:

1. **Business understanding** — Know the domain (banking, healthcare, etc.)
2. **Technical depth** — Architecture, security, performance, testing
3. **Process discipline** — CI/CD, code reviews, incident response
4. **Communication** — Between squads, with stakeholders, in post-mortems
5. **Continuous learning** — The ecosystem evolves every year

The difference between a "good" app and an "enterprise-grade" app is not
the features — it's the reliability, security, observability, and the
team's ability to ship confidently every two weeks.

---

*This document covers the complete lifecycle of an enterprise Android app,*
*from initial discovery to production operations. Each phase builds on the*
*previous one, and the patterns described here are used by companies like*
*banking apps, healthcare platforms, and fintech products serving millions.*
