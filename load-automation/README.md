# Load Automation (JMeter)

JMeter-based load tests for the **n11.com search module** (autocomplete API, search results, pagination).

## Requirements

- Maven 3.9+
- JDK 8-17 (jmeter-maven-plugin supports up to Java 17)

## Project Structure

```
load-automation/
├── pom.xml
├── docs/
│   └── N11_SEARCH_LOAD_TEST_SCENARIOS.md   # Detailed test scenarios
└── src/test/jmeter/
    ├── N11SearchLoad.jmx      # Main test plan
    ├── jmeter.properties      # Default parameters (threads, rampUp)
    └── search_keywords.csv    # Search keywords (CSV feeder)
```

## Test Plan: N11SearchLoad.jmx

**Goal:** Measure the performance of n11.com's header search box, autocomplete API, and search results under load.

**Flow (per virtual user):**

| Step | Request | Description |
|------|---------|-------------|
| 01 | `GET /` | Homepage — establish session and cookies |
| 02 | `GET /rest/v1/searchAutoCompleteService?q={keyword}&state=f` | Autocomplete API call |
| 03 | `GET /arama?q={keyword}` | Search results — page 1 |
| 04 | `GET /arama?q={keyword}&pg=2` | Pagination — page 2 |

Think times (1–2.5 s) are placed between steps to simulate real user behavior.

**Keywords:** Each thread picks the next keyword from `search_keywords.csv` via CSV Data Set Config (`shareMode.all`, `recycle=true`). With multiple threads and loops, this distributes different keywords across requests, minimizing cache effects.

**Scenario documentation:** See [docs/N11_SEARCH_LOAD_TEST_SCENARIOS.md](docs/N11_SEARCH_LOAD_TEST_SCENARIOS.md) for all scenarios (single search, pagination, mixed terms, peak, endurance).

## Running

```bash
cd load-automation

# Default (3 threads, 2 s ramp-up, random keywords from CSV)
mvn clean verify

# Override parameters
mvn clean verify -Dthreads=5 -DrampUp=3

# Run JMeter tests only (generates report, does not fail the build)
mvn jmeter:jmeter
```

## JMeter GUI

Open the test plan in JMeter GUI for editing:

```bash
mvn jmeter:configure jmeter:gui
```

Then open `src/test/jmeter/N11SearchLoad.jmx` to edit and save.

## Viewing Results

### 1. Raw Results (CSV)
- **Location:** `target/jmeter/results/N11SearchLoad.csv`
- Columns: `timeStamp`, `elapsed`, `label`, `responseCode`, `success`, `URL`, `Latency`, `Connect`, etc.
- Open in Excel/Numbers to filter by label and calculate average response time and error rate.

### 2. HTML Dashboard
- **Location:** `target/jmeter/reports/index.html`
- Open in browser: charts, summary table, response time distribution, error rate — all in one page.

### 3. Live via JMeter GUI
```bash
mvn jmeter:configure jmeter:gui
```
Open `N11SearchLoad.jmx` → **Run** → use Summary Report and View Results Tree for real-time results.

## Reports Summary

| Location | Content |
|----------|---------|
| `target/jmeter/results/*.csv` | Raw results (one row per request) |
| `target/jmeter/reports/index.html` | HTML dashboard (charts + summary) |

## Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| threads   | 3       | Concurrent virtual users |
| rampUp    | 2       | Time (seconds) for all threads to start |

Keywords are distributed from `search_keywords.csv` via CSV Data Set Config (sequential, recycled).

Override: `mvn verify -Dthreads=5 -DrampUp=3`

> **Note:** Only `N11SearchLoad.jmx` is executed (`testFilesIncluded` in `pom.xml`).
