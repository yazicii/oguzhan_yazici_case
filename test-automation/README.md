# Insider Test Automation - Case Study

Selenium WebDriver + TestNG + Allure-based UI test automation project.  
**Target:** Functional verification of [useinsider.com](https://useinsider.com) home page and careers module (QA job listings).

---

## Technologies

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 23 | Language |
| Selenium | 4.25.0 | Browser automation |
| TestNG | 7.10.2 | Test framework + parallel execution |
| Allure | 2.29.0 | Reporting (step, screenshot, log attachment) |
| Maven | 3.x | Build and dependency management |
| Log4j 2 | 2.24.3 | Logging |

## Architecture

```
src/
├── main/java/org/insider/
│   ├── config/ConfigManager.java         — Central configuration (env, timeout)
│   ├── data/JobCardInfo.java             — Job card info DTO (title, location, department)
│   ├── driver/WebDriverFactory.java      — Chrome/Firefox driver factory
│   ├── pages/
│   │   ├── BasePage.java                 — Shared driver + WaitHelper + PageFactory
│   │   ├── homepage/HomePage.java        — Home page POM
│   │   └── careers/
│   │       ├── QualityAssurancePage.java  — QA careers page POM
│   │       ├── JobListingsPage.java       — Job listings POM (filter, verify, View Role)
│   │       └── LeverJobPage.java          — Lever detail page POM
│   └── utils/WaitHelper.java             — Explicit wait utility
│
└── test/java/org/insider/
    ├── BaseTest.java                     — setUp/tearDown (ThreadLocal driver)
    ├── listeners/
    │   ├── AllureFailureListener.java    — Failure screenshot + log attachment
    │   └── AllureLogAppender.java        — Log4j -> Allure log collector
    ├── data/CareersDataProvider.java     — TestNG DataProvider (qaJobFilters)
    ├── homepage/HomePageTests.java       — Home page tests
    └── careers/CareersPageTests.java     — Careers page tests
```

### Design Decisions

- **Page Object Model:** Each page has its own class; locators are `@FindBy` fields and `static final By` constants for waits; business logic lives in methods.
- **ThreadLocal Driver:** Each thread uses its own browser in parallel execution; isolation guaranteed.
- **Explicit Wait Only:** `implicitlyWait(0)`. All waits go through `WaitHelper`; no mixing.
- **CSS Selector:** No text-based locators; immune to text changes.
- **Config Driven:** `config.properties` + `-D` override; environment-based URL support.

## Test Scenarios

### HomePageTests

| Test | Description | Severity |
|------|-------------|----------|
| `verifyHomePageOpensAndAllBlocksLoad` | Verifies home page opens, URL/title validation, all main sections (hero, social proof, features, etc.) load correctly | BLOCKER |

### CareersPageTests

| Test | Description | Severity |
|------|-------------|----------|
| `filterQAJobsByLocationAndDepartmentAndCheckPresence` | QA page -> See all QA jobs -> Filter by location -> Verify all cards match Position/Department/Location criteria | CRITICAL |
| `viewRoleRedirectsToLeverApplicationPage` | QA page -> See all QA jobs -> Filter -> Click View Role -> Verify redirect to Lever.co and match job details | CRITICAL |

## Running

```bash
# Default (Chrome, visible, sequential)
mvn clean test

# Firefox + headless
mvn clean test -Dbrowser=firefox -Dheadless=true

# Parallel execution (test-based)
mvn clean test -Pparallel-tests

# Parallel execution (method-based, max 4 threads)
mvn clean test -Pparallel-methods

# Sequential execution
mvn clean test -Psequential

# Specific test
mvn clean test -Dtest=CareersPageTests#filterQAJobsByLocationAndDepartmentAndCheckPresence
```

## Parallel Execution Profiles

TestNG suite files are in `src/test/resources/testng/`:

| Profile | TestNG Suite | Description |
|---------|-------------|-------------|
| *(default)* | `testng.xml` | `parallel="tests"`, 2 threads |
| `-Pparallel-tests` | `testng-parallel-tests.xml` | Each `<test>` in its own thread |
| `-Pparallel-classes` | `testng-parallel-classes.xml` | Each test class in its own thread |
| `-Pparallel-methods` | `testng-parallel-methods.xml` | Each `@Test` method in its own thread (4 threads) |
| `-Psequential` | `testng-sequential.xml` | No parallelism |

## Allure Report

Auto-open is controlled from a **single point**: `config.properties` -> `skip.open.allure`

| Value | Behavior |
|-------|----------|
| `false` | Report opens automatically in browser after tests |
| `true` | Report is generated but browser does not open (CI) |

```bash
# Tests + report + auto-open (if skip.open.allure=false)
mvn clean test

# Command-line override
mvn clean test -Dskip.open.allure=false
```

Manual open: `mvn allure:serve` (or `target/site/allure-maven-plugin/index.html`).

Each test in the report includes:
- Step-by-step action log (`Allure.step`)
- Assertion results
- Screenshot on failure (PNG)
- Test logs (TXT attachment)
- `@Feature`, `@Story`, `@Severity`, `@Description` metadata

## Configuration

`src/test/resources/config.properties` — override via `mvn -Dkey=value`:

| Key | Default | Description |
|-----|---------|-------------|
| `browser` | chrome | Browser (chrome, firefox) |
| `headless` | false | Headless mode |
| `env` | production | Environment (production, staging, test) |
| `timeout.default` | 15 | Default wait timeout (seconds) |
| `timeout.short` | 5 | Short wait timeout (seconds) |
| `timeout.long` | 30 | Long wait timeout (seconds) |
| `skip.open.allure` | false | Auto-open Allure report |
