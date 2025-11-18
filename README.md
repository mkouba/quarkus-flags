# Quarkus Feature Flags

This projects aims to provide a lightweight and extensible feature flag Quarkus extension.
More specifically, it provides:

* An API to access feature flags.
* An SPI to provide flags and externalize the computation of a flag value.
* Several built-in flag providers 
   * Leverage Quarkus config to define feature flags,
   * In-memory repository (useful for testing and dynamic registration).
* *JPA module*, where feature flags are mapped from an annotated entity and are automatically loaded from the database.
* *Security module*, so that it's possible to evaluate flags based on the current `SecurityIdentity`.
* *Qute module* so that it's possible to use the flags directly in templates.
