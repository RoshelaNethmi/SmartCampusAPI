# Smart Campus Sensor & Room Management API
### 5COSC022W Client-Server Architectures — University of Westminster

## Overview
A RESTful API built with JAX-RS (Jersey 2.32) on Apache Tomcat 9.
Manages campus rooms and sensors with full CRUD, filtering,
sub-resource readings, and enterprise error handling.

**Base URL:** `http://localhost:8080/SmartCampusAPI/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1 | API discovery |
| GET/POST | /api/v1/rooms | List / create rooms |
| GET/DELETE | /api/v1/rooms/{id} | Get / delete room |
| GET/POST | /api/v1/sensors | List / create sensors |
| GET | /api/v1/sensors?type=X | Filter sensors by type |
| GET/POST | /api/v1/sensors/{id}/readings | List / add readings |

---

## Build and Run Instructions

### Prerequisites
- Java JDK 8 or later
- Apache NetBeans 12+
- Apache Tomcat 9.x
- Maven (included with NetBeans)

### Steps
1. Clone this repository:
   ```bash
   git clone https://github.com/YOURUSERNAME/SmartCampusAPI.git
   ```
2. Open NetBeans → File → Open Project → select the cloned folder
3. Right-click project → Clean and Build
4. Right-click project → Run (starts Tomcat automatically)
5. API is live at: http://localhost:8080/SmartCampusAPI/api/v1

---

## Sample curl Commands

```bash
# 1. API Discovery
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1

# 2. Get all rooms
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms

# 3. Create a room
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"HALL-201","name":"Main Hall","capacity":200}'

# 4. Filter sensors by type
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2

# 5. Post a sensor reading
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":25.3}'
```

---

## Report — Question Answers

### Part 1 Q1 — JAX-RS Resource Lifecycle
By default, JAX-RS creates a new instance of every resource class for
each incoming HTTP request (per-request scope). Instance variables are
not shared between requests. This is why all data in this project is
stored in static fields in DataStore.java — static fields belong to the
class, not the instance, so they persist across all requests. Without
static storage, data added in one request would be lost before the next.

### Part 1 Q2 — HATEOAS
HATEOAS means API responses include hyperlinks describing what the
client can do next. Instead of clients hardcoding URLs from docs, they
discover them from responses. This reduces coupling — if the server
changes a URL, only the link in the response changes, not every client.

### Part 2 Q1 — IDs vs Full Objects
Returning only IDs saves bandwidth but forces N+1 requests — one per
room to get details. Returning full objects costs more data upfront but
gives clients everything in one round trip, which is preferred for
typical use cases.

### Part 2 Q2 — DELETE Idempotency
Yes, DELETE is idempotent. The first call removes the room (204). A
second call finds nothing and returns 404. The server state after both
is identical — the room does not exist.

### Part 3 Q1 — @Consumes and Media Type Mismatch
@Consumes(APPLICATION_JSON) tells JAX-RS to only accept requests with
Content-Type: application/json. If a client sends text/plain, JAX-RS
rejects the request automatically with 415 Unsupported Media Type,
before the method body even runs.

### Part 3 Q2 — @QueryParam vs Path Segment for Filtering
Query parameters are semantically correct for filtering — they are
optional modifiers on a collection, not part of the resource identity.
A path like /sensors/type/CO2 implies type is a resource, not a filter.
Omitting a query param (GET /sensors) naturally returns everything.

### Part 4 Q1 — Sub-Resource Locator Pattern
The pattern delegates a URL sub-path to a separate class. Without it,
SensorResource would contain every reading method too, growing to
hundreds of lines. Each class stays focused on one concern, is easier
to test, and adding new sub-resources only requires one new class.

### Part 5 Q1 — 422 vs 404 for Missing Reference
404 means the URL does not identify a resource. 422 means the URL is
valid but the JSON payload is semantically wrong. When roomId does not
exist, the URL /sensors is valid — the problem is inside the body.
422 communicates this precisely.

### Part 5 Q2 — Stack Trace Security Risk
Stack traces expose class names, method names, line numbers, and library
versions. Attackers use this to find known vulnerabilities in specific
versions. The GlobalExceptionMapper logs the trace on the server only
and returns a generic message to the client.

### Part 5 Q3 — Filters vs Manual Logging
A filter applies automatically to every request. Manual Logger calls
must be added to every method — easy to forget when adding new
endpoints, and produces inconsistent logs. One filter class enforces
uniform observability with zero changes to resource classes.
