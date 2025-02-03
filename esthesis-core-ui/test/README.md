# Front-end tests

Front-end tests are written using [k6](https://k6.io/).

## Running tests with or without a browser UI
You can execute the tests while having a tests-driven browser interface on your screen. This is useful when you are writing new tests, or debugging existing ones. To do so, set `K6_BROWSER_HEADLESS=false K6_BROWSER_ENABLED=true` before running `k6`.

## Taking schreenshots
By default, test get executed without taking screenshots. If you want to enable screenshots, set `SCREENSHOTS_ACTIVE=true` before running `k6`.

## Running tests
```bash
  k6 run k6-test.js
```
