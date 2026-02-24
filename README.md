# Oguzhan Yazici - QA Automation Case Study

Comprehensive test automation portfolio covering **UI testing**, **API testing**, and **load testing**.

## Projects

| Project | Technology | Target | Description |
|---------|-----------|--------|-------------|
| [test-automation](test-automation/) | Selenium + TestNG + Allure | [useinsider.com](https://useinsider.com) | UI functional tests for home page and QA careers module |
| [api-automation](api-automation/) | REST Assured + TestNG + Allure | [Petstore API](https://petstore.swagger.io) | CRUD API tests for the Pet endpoint (27 tests) |
| [load-automation](load-automation/) | JMeter + Maven | [n11.com](https://www.n11.com) | Load tests for the search module (autocomplete, results, pagination) |

## Quick Start

```bash
# UI Tests (default: Chrome, visible)
cd test-automation && mvn clean test

# API Tests
cd api-automation && mvn clean test

# Load Tests (3 threads, 2s ramp-up)
cd load-automation && mvn clean verify
```

## Requirements

- Java 23
- Maven 3.9+
- Chrome or Firefox (for UI tests)

## Reports

- **UI & API Tests:** Allure reports auto-open in browser after execution
- **Load Tests:** HTML dashboard at `target/jmeter/reports/N11SearchLoad/index.html`
