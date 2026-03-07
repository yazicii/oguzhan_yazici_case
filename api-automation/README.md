# API Automation (REST Assured)

REST Assured-based API test automation for the **Petstore Swagger API** (`/pet` endpoints). Covers full CRUD operations with positive and negative test scenarios.

## Requirements

- Maven 3.9+
- JDK 23

## Project Structure

```
api-automation/
├── pom.xml
├── scripts/
│   └── open-allure-report.sh          # Auto-opens Allure report in browser
└── src/
    ├── main/java/org/insider/
    │   ├── config/ApiConfig.java       # Configuration manager (config.properties)
    │   ├── client/PetApiClient.java    # REST client for /pet endpoints
    │   └── models/
    │       ├── Pet.java                # Pet model with builder pattern
    │       ├── Category.java           # Category model
    │       └── Tag.java                # Tag model
    └── test/
        ├── java/org/insider/
        │   ├── BaseApiTest.java        # Base class: REST Assured setup, Allure filter
        │   └── pet/
        │       ├── CreatePetTests.java # POST /pet (8 tests)
        │       ├── ReadPetTests.java   # GET /pet/{id}, /pet/findByStatus (7 tests)
        │       ├── UpdatePetTests.java # PUT /pet (6 tests)
        │       └── DeletePetTests.java # DELETE /pet/{id} (5 tests)
        └── resources/
            ├── config.properties       # API URL, Allure settings, logging
            ├── allure.properties       # Allure results directory
            ├── log4j2-test.xml         # Log4j2 configuration
            └── testng/
                ├── testng.xml                    # Default suite (CRUD order)
                └── testng-parallel-methods.xml   # Parallel by method (profile: -Pparallel-methods)
```

## Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| REST Assured | 5.5.0 | HTTP client for API testing |
| TestNG | 7.10.2 | Test framework |
| Allure | 2.29.0 | Test reporting with request/response capture |
| Jackson | 2.18.2 | JSON serialization/deserialization |
| Log4j2 | 2.24.3 | Logging |
| Maven Surefire | 3.2.5 | Test execution |

## Test Scenarios (27 total)

**Note on negative tests:** `createPetWithEmptyBody` and `createPetWithNegativeId` assert that the API rejects invalid input (expect 4xx). The public Petstore API returns 200 for these cases—a known permissive behavior. These tests document the expected contract; against a properly validating API they would pass.

### Create Pet (8 tests)

| Test | Type | Description |
|------|------|-------------|
| createPetWithAllFields | Positive | All fields populated |
| createPetWithRequiredFieldsOnly | Positive | Only name + photoUrls |
| createPetWithPendingStatus | Positive | status=pending |
| createPetWithSoldStatus | Positive | status=sold |
| createPetWithInvalidJson | Negative | Invalid JSON body → 4xx |
| createPetWithEmptyBody | Negative | Empty `{}` (missing required fields) → expect 4xx |
| createPetWithNegativeId | Negative | id=-1 → expect 4xx |
| createPetWithVeryLongName | Negative | 1000-character name |

### Read Pet (8 tests)

| Test | Type | Description |
|------|------|-------------|
| getPetByValidId | Positive | GET by existing ID |
| findPetsByAvailableStatus | Positive | findByStatus=available |
| findPetsByPendingStatus | Positive | findByStatus=pending |
| findPetsBySoldStatus | Positive | findByStatus=sold |
| getPetByNonExistentId | Negative | Non-existent ID -> 404 |
| getPetByInvalidStringId | Negative | String ID ('abc') |
| getPetByZeroId | Negative | ID=0 query |
| findPetsByInvalidStatus | Negative | Invalid status value |

### Update Pet (6 tests)

| Test | Type | Description |
|------|------|-------------|
| updatePetName | Positive | Change pet name |
| updatePetStatus | Positive | available -> sold |
| updatePetCategoryAndTags | Positive | Update category + tags |
| fullUpdateAllFields | Positive | Change all fields at once |
| updateNonExistentPet | Negative | Non-existent ID |
| updatePetWithInvalidBody | Negative | Invalid JSON body |

### Delete Pet (5 tests)

| Test | Type | Description |
|------|------|-------------|
| deleteExistingPet | Positive | Delete + verify 404 |
| deletePetVerifyNotFoundMessage | Positive | Verify 'Pet not found' message |
| createDeleteRecreateLifecycle | Positive | Full lifecycle test |
| deleteNonExistentPet | Negative | Delete non-existent -> 404 |
| deleteSamePetTwice | Negative | Double delete -> 404 |

## Architecture

- **Client Layer** (`PetApiClient`): Wraps all HTTP calls. Tests never call REST Assured directly.
- **Model Layer** (`Pet`, `Category`, `Tag`): Jackson-annotated POJOs with builder pattern.
- **Config Layer** (`ApiConfig`): Loads `config.properties` with system property override support.
- **Base Test** (`BaseApiTest`): Sets up request spec, Allure filter, and optional logging.
- **Test Isolation**: Each test generates a unique pet ID via `uniquePetId()` to prevent collisions.

## Running

```bash
cd api-automation

# Run all tests (Allure report auto-opens)
mvn clean test

# Run with custom API URL
mvn clean test -Dapi.base.url=https://custom.api/v2

# Skip Allure report auto-open
mvn clean test -Dskip.open.allure=true

# CI mode (no browser)
mvn clean test -Pci

# Parallel execution (methods)
mvn clean test -Pparallel-methods
```

## Allure Report

After test execution, the Allure report opens automatically in the browser (configurable via `skip.open.allure` in `config.properties`).

Manual generation:
```bash
mvn allure:report
mvn allure:serve
```

## Configuration

All settings in `src/test/resources/config.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| api.base.url | https://petstore.swagger.io/v2 | API base URL |
| skip.open.allure | false | Auto-open Allure report after tests |
| log.level | INFO | Logging level |
| api.log.requests | false | Print request/response to console |
