# n11.com Search Module - Load Test Scenarios

**Goal:** Investigate the behaviour of the **search module** (header search) and **search results listing** on https://www.n11.com/ under load.

---

## 1. Identified Endpoints (from browser Network tab)

| Step | Endpoint | Method | Description |
|------|----------|--------|-------------|
| **Autocomplete** | `https://www.n11.com/rest/v1/searchAutoCompleteService?q={keyword}&state=f` | GET | Returns autocomplete suggestions as the user types |
| **Search results** | `https://www.n11.com/arama?q={keyword}` | GET | Full search results page after pressing Enter or selecting a suggestion |
| **Pagination** | `https://www.n11.com/arama?q={keyword}&pg={page}` | GET | Search results page 2, 3, etc. |

---

## 2. Test Scenarios

### Scenario 1: Autocomplete API - response time under load

| Item | Detail |
|------|--------|
| **Purpose** | Measure the autocomplete API response time when users type in the header search box |
| **Endpoint** | `GET /rest/v1/searchAutoCompleteService?q={keyword}&state=f` |
| **Steps** | 1. Open homepage (session). 2. Send a short keyword to the autocomplete API (e.g., "tel", "lap"). |
| **Success** | HTTP 200, p95 < 500 ms, error rate < 1% |
| **Load** | 30-50 concurrent users, 5 min |
| **Metrics** | Response time (min/avg/p90/p95/max), throughput (req/s), error rate |

---

### Scenario 2: Header search - full keyword submission

| Item | Detail |
|------|--------|
| **Purpose** | Measure search results page performance when a user submits a full keyword |
| **Endpoint** | `GET /arama?q={keyword}` |
| **Steps** | 1. Open homepage. 2. Autocomplete call (for realism). 3. Load full search results page. |
| **Success** | HTTP 200, p95 < 3 s, response body contains product listings |
| **Load** | 20-50 concurrent users, 5-10 min |
| **Metrics** | Response time, throughput, error rate |

---

### Scenario 3: Search results - pagination (page 2, 3)

| Item | Detail |
|------|--------|
| **Purpose** | Measure pagination performance on search results |
| **Endpoint** | `GET /arama?q={keyword}&pg=2`, `&pg=3` |
| **Steps** | 1. Perform search. 2. Load first results page. 3. Navigate to page 2. 4. Navigate to page 3. |
| **Success** | HTTP 200, pagination pages should not be significantly slower than page 1 |
| **Load** | 20-40 concurrent users; all users perform pagination |
| **Metrics** | Response time per page (1 vs 2 vs 3), error rate |

---

### Scenario 4: Realistic user flow - autocomplete + search + pagination

| Item | Detail |
|------|--------|
| **Purpose** | Simulate a realistic user flow: type -> autocomplete -> search -> page 2 |
| **Endpoints** | Autocomplete -> Search -> Pagination (sequential) |
| **Steps** | 1. `GET /` (session). 2. `GET autocomplete`. 3. `GET /arama?q=telefon`. 4. `GET /arama?q=telefon&pg=2`. |
| **Success** | HTTP 200 on every step |
| **Load** | 30-50 concurrent users, 10 min, think time 1-3 s between steps |
| **Metrics** | Per-request response time, overall throughput |

---

### Scenario 5: Mixed search terms (cache effect minimization)

| Item | Detail |
|------|--------|
| **Purpose** | Use different search terms to reduce cache effects and create a realistic load |
| **Data** | CSV feeder: telefon, laptop, kulaklik, robot supurge, oyuncu koltugu, tv, bulasik makinesi |
| **Steps** | Each user picks a random keyword from CSV -> autocomplete + search + pagination |
| **Success** | No keyword should show significantly worse performance than others |
| **Load** | 30-50 concurrent users, 10 min |
| **Metrics** | Response time per keyword, throughput, error rate |

---

### Scenario 6: Peak load / stress

| Item | Detail |
|------|--------|
| **Purpose** | Find the breaking point and performance degradation threshold |
| **Steps** | Same flow (autocomplete + search + pagination), high load |
| **Load** | 100-200 concurrent users, 1 min ramp-up, 5-10 min sustain |
| **Success** | p95 < 5 s acceptable; 5xx rate < 5% |
| **Metrics** | Response time, error rate, throughput (breaking point) |

---

## 3. Test Data (CSV Feeder)

File: `src/test/jmeter/search_keywords.csv`

```csv
keyword
telefon
laptop
kulaklik
robot supurge
oyuncu koltugu
tv
bulasik makinesi
```

Keywords are distributed sequentially via CSV Data Set Config (`shareMode.all`, `recycle=true`). With multiple threads and loops, different keywords are used across requests to minimize cache effects.

---

## 4. JMeter Test Plan Summary

**File:** `src/test/jmeter/N11SearchLoad.jmx`

```
Thread Group (n11 Search Users)
  ├── HTTP Request Defaults (www.n11.com, HTTPS)
  ├── HTTP Header Manager (User-Agent, Accept, Accept-Language)
  ├── HTTP Cookie Manager
  ├── CSV Data Set Config (search_keywords.csv -> ${keyword}, sequential, recycled)
  │
  ├── 01 Homepage GET /
  │     └── Response Assertion (HTTP 200)
  ├── Think Time (1 s)
  ├── 02 Autocomplete GET /rest/v1/searchAutoCompleteService?q=${keyword}&state=f
  │     └── Response Assertion (HTTP 200)
  ├── Think Time (1.5 s)
  ├── 03 Search Results GET /arama?q=${keyword}
  │     └── Response Assertion (HTTP 200)
  ├── Think Time (2.5 s)
  ├── 04 Pagination GET /arama?q=${keyword}&pg=2
  │     └── Response Assertion (HTTP 200)
  │
  ├── Summary Report
  └── View Results Tree (disabled by default)
```

---

## 5. Running

```bash
cd load-automation

# Default (3 users, 2 s ramp-up, random keywords from CSV)
mvn clean verify

# Override parameters
mvn clean verify -Dthreads=30 -DrampUp=10

# JMeter GUI for editing
mvn jmeter:configure jmeter:gui
```

---

## 6. Scenario Summary Table

| ID | Scenario | Endpoint(s) | Load |
|----|----------|-------------|------|
| 1 | Autocomplete API | `/rest/v1/searchAutoCompleteService` | 30-50 users |
| 2 | Header search | `/arama?q=` | 20-50 users |
| 3 | Pagination | `/arama?q=&pg=2,3` | 20-40 users |
| 4 | Realistic flow | All three sequential | 30-50 users |
| 5 | Mixed keywords | CSV feeder + all | 30-50 users |
| 6 | Peak / stress | All, high load | 100-200 users |
