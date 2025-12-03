# Quarkus Feature Flags

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.flags/quarkus-flags-parent?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.flags/quarkus-flags-parent)

This project aims to provide a lightweight and extensible feature flag Quarkus extension.
More specifically, it provides:

* An API to access feature flags.
* An SPI to provide flags and externalize the computation of a flag value.
* Built-in flag providers:
   * Leverage Quarkus config to define feature flags,
   * In-memory repository (useful for testing and dynamic registration).
* Built-in flag evaluators:
   * Time span evaluator - based on the current date-time obtained from the system clock in the default time-zone.
   * Composite evaluator - evaluates a flag with the specified sub-evaluators.
* *Hibernate ORM module*, where feature flags are mapped from an annotated entity and are automatically loaded from the database.
* *Security module*, so that it's possible to evaluate flags based on the current `SecurityIdentity`.
* *Qute module* so that it's possible to use the flags directly in templates.

## Documentation

The documentation is available at https://docs.quarkiverse.io/quarkus-flags/dev/.
