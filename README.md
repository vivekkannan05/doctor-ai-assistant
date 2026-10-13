# Doctor's Assistant — AI-Powered Clinic Chatbot

A proof-of-concept CLI chatbot that acts as a friendly receptionist at **Super Clinic**. It uses **OpenAI's Responses API with Tool Calling** to answer natural language queries about doctors, specialties, availability, and appointment booking — all backed by a real database.

> **Key concept:** The LLM is _not_ trained on clinic data. Instead, it's given a persona via a system prompt and a set of tools (Java functions) it can call to look up real data in the database. The LLM decides _when_ and _which_ tool to call based on the conversation.

---

## Architecture Overview

```
┌─────────────┐     ┌──────────────────────┐     ┌──────────────┐
│  CLI (You)  │────▶│ ChatbotOrchestrator  │────▶│  OpenAI API  │
│             │◀────│  (conversation loop) │◀────│  (GPT-4o)    │
└─────────────┘     └──────────┬───────────┘     └──────────────┘
                               │                        │
                    ┌──────────▼───────────┐            │
                    │   ToolDispatcher     │    tool calls returned
                    │  (routes tool calls) │◀───────────┘
                    └──────────┬───────────┘
                               │
                    ┌──────────▼───────────┐
                    │    ClinicService     │
                    │  (business logic)    │
                    └──────────┬───────────┘
                               │
                    ┌──────────▼───────────┐
                    │  H2 Database         │
                    │  (Panache ORM)       │
                    └──────────────────────┘
```

### Request flow for a single user message:

1. User types a message → `DoctorAssistantApp` reads it
2. `ChatbotOrchestrator` sends it to OpenAI with the system prompt + tool definitions
3. OpenAI responds with either:
   - **Text** → display to user, done
   - **Tool call** (e.g., `GetDoctorsBySpecialty("Orthopedics")`) → continue to step 4
4. `ToolDispatcher` routes the tool call to the matching `ClinicService` method
5. `ClinicService` queries the H2 database and returns results
6. Results are sent back to OpenAI → the LLM generates a natural language response
7. Steps 3–6 may repeat if the LLM chains multiple tool calls in one turn

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Quarkus 3.22 (Command Mode — CLI, not web server) |
| LLM API | OpenAI Responses API (GPT-4o) with Tool Calling |
| OpenAI SDK | `com.openai:openai-java:4.45.0` |
| ORM | Hibernate ORM with Panache |
| Database | H2 (embedded, file-based) |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/clinic/assistant/
├── DoctorAssistantApp.java          # CLI entry point (outer loop)
├── chatbot/
│   ├── ChatbotOrchestrator.java     # Conversation loop + OpenAI API calls
│   ├── ToolClasses.java             # Tool definitions (JSON schemas for LLM)
│   └── ToolDispatcher.java          # Routes tool calls → ClinicService
├── model/
│   ├── Doctor.java                  # Doctor entity (name, specialty)
│   ├── AvailabilitySlot.java        # Time slot entity (date, time, booked?)
│   └── Appointment.java             # Booked appointment record
└── service/
    └── ClinicService.java           # Business logic (6 tool functions)

src/main/resources/
├── application.properties           # Database + OpenAI config
└── import.sql                       # Seed data (doctors + slots)
```

---

## Class-by-Class Guide

### `DoctorAssistantApp.java` — CLI Entry Point

The **outer loop** of the application. Uses Quarkus **Command Mode** (`@QuarkusMain`) to run as a CLI app instead of a web server.

**What it does:**
- Prints the welcome banner
- Calls `orchestrator.startConversation()` to get the greeting
- Loops: reads user input → sends to `ChatbotOrchestrator` → prints the response
- Exits on `quit`, `exit`, or `bye`

**Why Command Mode?** This is a POC meant to run in a terminal. No REST endpoints, no web UI — just a simple `Scanner` reading from stdin.

---

### `ChatbotOrchestrator.java` — The Conversation Loop

The **beating heart** of the chatbot. Manages the full interaction cycle with OpenAI.

**What it does:**
1. Builds each request with: system prompt + tool definitions + user message
2. Sends to OpenAI's Responses API
3. If the response contains **tool calls**, executes them via `ToolDispatcher` and sends results back (inner loop)
4. If the response contains **text**, returns it for display
5. Tracks token usage per turn and per session

**Key design decisions:**

| Concept | How it works |
|---|---|
| **`previousResponseId`** | Each response ID is saved. On the next turn, it's passed to OpenAI so the server remembers the full conversation — no need to resend the entire history |
| **System prompt** | A ~350-token "employee handbook" that defines the receptionist persona, booking rules, symptom handling, tool usage instructions, and boundaries |
| **Token tracking** | Logs input/output tokens for every API call, with per-turn and session-cumulative summaries |
| **Inner loop** | After a tool call, only the tool _results_ are sent back (not the tool calls themselves), using the current response's ID as context |
| **Max tool rounds** | Capped at 10 to prevent infinite loops |

---

### `ToolClasses.java` — Tool Definitions

Defines the **6 tools** the LLM can call. Each inner class becomes a JSON schema that OpenAI reads to understand what tools are available and what parameters they accept.

**How it works:**
- `@JsonClassDescription` → becomes the tool's description (the LLM reads this to decide _when_ to use it)
- `@JsonPropertyDescription` → describes each parameter (the LLM reads this to fill in values)
- `public` fields → become parameters (Java type → JSON type)

The LLM never sees this Java code — it only sees the auto-generated JSON schema.

**The 6 tools:**

| Tool Class | Parameters | Purpose |
|---|---|---|
| `GetAllDoctors` | — | List all doctors with their specialties |
| `GetDoctorByName` | `doctorName` | Find a specific doctor by name |
| `GetAllSpecialties` | — | List all available specialties at the clinic |
| `GetDoctorsBySpecialty` | `specialty` | Find doctors for a given specialty |
| `GetAvailabilities` | `doctorName`, `date` | Check a doctor's open time slots on a date |
| `BookAppointment` | `doctorName`, `date`, `time`, `patientName` | Book an appointment (requires prior patient confirmation) |

---

### `ToolDispatcher.java` — Tool Call Router

Routes the LLM's function call requests to the actual `ClinicService` methods.

**What it does:**
1. Reads the function name from the LLM's response (e.g., `"GetDoctorsBySpecialty"`)
2. Deserializes the JSON arguments into the matching `ToolClasses` inner class
3. Calls the corresponding `ClinicService` method
4. Returns the result (serialized back to JSON for the LLM)
5. Wraps everything in try/catch — errors are returned as structured JSON to the LLM, not app crashes

---

### `Doctor.java` — Doctor Entity

JPA entity mapped to the `doctor` table. Uses **Panache** (Quarkus's Active Record pattern) — no separate Repository class needed.

**Fields:** `name`, `specialty` (plus auto-generated `id` from `PanacheEntity`)

**Query methods:**
- `findBySpecialty(String)` — case-insensitive specialty match
- `findByNameContaining(String)` — fuzzy name search (e.g., "Sharma" matches "Dr. Sharma")
- `findAllSpecialties()` — distinct specialty list

---

### `AvailabilitySlot.java` — Time Slot Entity

Represents a bookable 30-minute slot for a doctor. Linked to `Doctor` via `@ManyToOne`.

**Fields:** `doctor`, `slotDate`, `startTime`, `endTime`, `isBooked`

**Query methods:**
- `findAvailable(doctorId, date)` — all unbooked slots for a doctor on a date, ordered by time
- `findSlot(doctorId, date, time)` — find a specific unbooked slot for booking

When an appointment is booked, `isBooked` is set to `true` so the slot no longer appears in availability queries.

---

### `Appointment.java` — Appointment Record

Created when `BookAppointment` succeeds. Stores the confirmed booking details.

**Fields:** `slot` (link to the booked slot), `doctor`, `patientName`, `patientPhone`, `appointmentDate`, `appointmentTime`, `bookedAt` (timestamp)

---

### `ClinicService.java` — Business Logic

The **6 tool functions** that query and update the database. This class knows **nothing about OpenAI** — it's pure clinic operations.

**All methods are `@Transactional`** (required in Quarkus Command Mode where there's no HTTP request context).

| Method | Tool | What it does |
|---|---|---|
| `getAllDoctors()` | GetAllDoctors | Returns all doctors as `[{name, specialty}, ...]` |
| `getDoctorByName(name)` | GetDoctorByName | Fuzzy name search, returns first match |
| `getAllSpecialties()` | GetAllSpecialties | Returns distinct specialty list |
| `getDoctorsBySpecialty(specialty)` | GetDoctorsBySpecialty | Returns doctors for a specialty, or "not available" message |
| `getAvailabilities(doctor, date)` | GetAvailabilities | Returns open time slots, or "no slots" message |
| `bookAppointment(doctor, date, time, patient)` | BookAppointment | Atomic two-step: marks slot as booked + creates appointment record |

---

### `application.properties` — Configuration

| Property | Value | Purpose |
|---|---|---|
| `quarkus.datasource.db-kind` | `h2` | Embedded database, no external server needed |
| `quarkus.datasource.jdbc.url` | `jdbc:h2:file:./data/clinicdb` | File-based persistence in `data/` folder |
| `quarkus.hibernate-orm.database.generation` | `drop-and-create` | Recreates tables on every startup (POC mode) |
| `openai.api.key` | `${OPENAI_API_KEY:not-set}` | Read from environment variable — never hardcoded |

---

### `import.sql` — Seed Data

Loaded automatically on startup. Seeds 6 doctors across 4 specialties and ~60 availability slots.

**Doctors:**

| Doctor | Specialty |
|---|---|
| Dr. Sharma | Orthopedics |
| Dr. Patel | Orthopedics |
| Dr. Rao | General Medicine |
| Dr. Gupta | Cardiology |
| Dr. Iyer | General Medicine |
| Dr. Nair | Neurology |

**Slot dates** use `DATEADD('DAY', n, CURRENT_DATE)` so they're always relative to today — no hardcoded dates that go stale.

---

## Prerequisites

- **Java 21** (Amazon Corretto recommended)
- **Maven 3.8+**
- **OpenAI API key** with billing enabled

---

## How to Run

```bash
# 1. Set your OpenAI API key
export OPENAI_API_KEY="sk-proj-your-key-here"

# 2. Set Java 21
export JAVA_HOME=/path/to/java-21

# 3. Build and run
cd doctor-assistant
mvn compile quarkus:dev -Dquarkus.console.enabled=false
```

You'll see:

```
===========================================
   Doctor's Assistant - Super Clinic
   Type 'quit' or 'exit' to end
===========================================
Assistant: Hello and welcome to the Super Clinic! How can I help you today?

You: _type your message here_
```

> `-Dquarkus.console.enabled=false` is required — without it, Quarkus dev mode captures your keyboard input.

---

## Sample Conversations

### Test 1 — Symptom-based routing

```
You: I fell down playing badminton, my ankle is swollen
     → LLM identifies "orthopedics", calls GetDoctorsBySpecialty("Orthopedics")
Assistant: I'd recommend seeing an orthopedic specialist. We have Dr. Sharma and Dr. Patel.
           Would you like to check their availability?
```

### Test 2 — Check availability and book

```
You: Check Dr. Sharma's availability today
     → LLM calls GetAvailabilities("Dr. Sharma", "2026-10-13")
Assistant: Dr. Sharma has the following slots today: 09:00-09:30, 09:30-10:00, 10:00-10:30...

You: Book the 9:00 slot, my name is Vijay
     → LLM calls BookAppointment("Dr. Sharma", "2026-10-13", "09:00", "Vijay")
Assistant: Appointment confirmed for Vijay with Dr. Sharma on 2026-10-13 at 09:00!
```

### Test 3 — Unavailable specialty

```
You: I've been having rashes for the past few days
     → LLM identifies "dermatology", calls GetDoctorsBySpecialty("Dermatology")
     → Tool returns: no doctors for that specialty
Assistant: I'm sorry, we don't have any dermatologists at our clinic.
           Would you like me to help with anything else?
```

### Test 4 — Out-of-scope query

```
You: What's the cost of a consultation?
Assistant: I can only help with appointment booking.
           Please contact our front desk for billing queries.
```

---

## Token Usage

The app logs token consumption for every API call. You'll see lines like:

```
[TOKENS] User message — input: 1045, output: 35, total: 1080
[TOKENS] Tool round 1 — input: 1120, output: 92, total: 1212
[TOKENS] Turn summary — API calls: 2, input: 2165, output: 127, turn total: 2292
[TOKENS] Session cumulative — input: 2165, output: 127, session total: 2292
```

**What makes up the input tokens on each call:**

| Component | ~Tokens | Sent every call? |
|---|---|---|
| System prompt | ~350 | Yes |
| Tool definitions (6 tools) | ~350 | Yes |
| Conversation history (via `previousResponseId`) | grows | Yes |
| New user message / tool results | varies | Yes |

**Cost estimate** (GPT-4o pricing):

| Scenario | Est. API calls | Est. tokens | Est. cost |
|---|---|---|---|
| Simple greeting + goodbye | 1 | ~1,000 | < $0.01 |
| Full booking (5-6 turns) | ~12 | ~15,000 | ~$0.04 |
| Long exploration (10+ turns) | ~25 | ~40,000 | ~$0.12 |

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| **CLI, not web** | Simplest way to demonstrate tool calling without frontend complexity |
| **H2 embedded database** | Zero setup — no Docker, no external DB server |
| **`drop-and-create` on startup** | POC mode — fresh data every run, dates always current |
| **`previousResponseId`** | Efficient multi-turn — server manages history, we only send new items |
| **Hardcoded greeting** | Saves ~1,000 tokens per startup vs. asking the LLM to say hello |
| **`@Transactional` on ClinicService** | Required in Command Mode (no HTTP request context for auto-transactions) |
| **Error → structured JSON** | Tool errors return `{error: "..."}` to the LLM so it can explain the issue naturally |

---

## License

This is a proof-of-concept project for learning purposes.
