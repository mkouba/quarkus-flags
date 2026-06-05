# Quarkus Feature Flags

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.flags/quarkus-flags-parent?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.flags/quarkus-flags-parent)

A lightweight and extensible feature flag extension for [Quarkus](https://quarkus.io).
Turn features on and off, roll out gradually, or switch behavior based on context — all with a simple and intuitive API.

## Quick Start

Add the dependency to your project:

```xml
<dependency>
    <groupId>io.quarkiverse.flags</groupId>
    <artifactId>quarkus-flags</artifactId>
    <version>LATEST</version>
</dependency>
```

Define a feature flag:

```properties
quarkus.flags.runtime."my-feature".value=true
```

Use it in your code:

```java
@Inject
Flags flags;

void doWork() {
    if (flags.isEnabled("my-feature")) {
        // feature is active
    }
}
```

## Features

* **Simple API** — inject `Flags` or individual `Flag` instances into any CDI bean.
* **Multiple value types** — boolean, string, integer, and decimal.
* **Built-in providers** — define flags via Quarkus config or use the in-memory repository for testing.
* **Built-in evaluators** — time span, composite, and variant evaluators out of the box.
* **Extensible SPI** — implement custom `FlagProvider` or `FlagEvaluator` to fit your needs.
* **[Hibernate ORM](https://docs.quarkiverse.io/quarkus-flags/dev/hibernate-orm.html)** and **[Hibernate Reactive](https://docs.quarkiverse.io/quarkus-flags/dev/hibernate-reactive.html)** — load flags from a database.
* **[Security](https://docs.quarkiverse.io/quarkus-flags/dev/security.html)** — evaluate flags based on the current `SecurityIdentity`, including percentage-based rollouts.
* **[Qute](https://docs.quarkiverse.io/quarkus-flags/dev/qute.html)** — use flags directly in templates.
* **[Cron](https://docs.quarkiverse.io/quarkus-flags/dev/cron.html)** — enable flags on a schedule using CRON expressions.
* **[OpenFeature](https://docs.quarkiverse.io/quarkus-flags/dev/openfeature.html)** — integrate with the [OpenFeature](https://openfeature.dev) standard.

## Documentation

Full documentation is available at https://docs.quarkiverse.io/quarkus-flags/dev/.
