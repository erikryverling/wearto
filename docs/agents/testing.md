# Testing Practices

Guidelines and frameworks for testing mobile and wear modules.

## Strategy

- **Fakes over Mocks:** Prefer creating lightweight in-memory fake implementations over mock frameworks for new and refactored tests.
- **Existing Tests:** Existing unit tests use MockK (`@RelaxedMockK`, `coEvery`, `coVerify`) and can be refactored toward fakes incrementally.

## Frameworks & Tooling

- **Runner:** JUnit Jupiter on the JUnit Platform (`org.junit.jupiter.api.Test`).
- **Assertions:** Kotest Assertions (`shouldBe`, `shouldContain`, etc.).
- **Flow Testing:** Turbine (`test { ... }`).
- **Mocking:** MockK (`io.mockk`).

## Scope & Conventions

- Focus unit tests on ViewModels, Repositories, and data/model mapping logic.
- Use `should`-format test names describing expected behavior under specific conditions.
