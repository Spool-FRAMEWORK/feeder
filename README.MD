# Spool — Publisher

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/build-Maven-C71A36.svg)](https://maven.apache.org/)

**Publisher** is the **Spool** framework module responsible for **publishing raw data from the inbox to event channels**. It acts as the bridge between inbox storage and downstream consumer distribution, ensuring traceability, idempotency, and declarative error handling.

---

## Table of Contents

- [Architecture](#architecture)
- [Key Concepts](#key-concepts)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Examples](#examples)
  - [Reactive Publishing](#reactive-publishing)
  - [Polling-based Publishing](#polling-based-publishing)
  - [Custom Polling Interval](#custom-polling-interval)
  - [Channel Routing by Event Type](#channel-routing-by-event-type)
  - [Custom Error Routing](#custom-error-routing)
  - [Full Pipeline (Crawler → Publisher → Ingester)](#full-pipeline-crawler--publisher--ingester)
- [API Reference](#api-reference)
- [Event Model](#event-model)
- [License](#license)

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                          Publisher                            │
│                                                              │
│  InboxItemStored ──► InboxItemStoredHandler                   │
│                          │                                   │
│                ┌─────────┼──────────┐                        │
│                ▼         ▼          ▼                        │
│         InboxUpdater  EventBus   InboxUpdater                │
│         (→PUBLISHING)  (emits    (→PUBLISHED)                │
│                       ItemPublished)                         │
│                                                              │
│  Strategy: ReactiveFeeder  OR  PollingFeeder                 │
│            (event-driven)      (scheduled poll)              │
│                                                              │
│  Errors ──► ErrorRouter ──► EventBus.emit(FailureEvent)      │
└──────────────────────────────────────────────────────────────┘
```

The publishing flow:

1. An **`InboxItemStored`** event is received (reactive) or discovered by polling the inbox.
2. The **`InboxUpdater`** marks the item as `PUBLISHING`.
3. An **`ItemPublished`** event is emitted on the event bus with the payload.
4. The **`InboxUpdater`** marks the item as `PUBLISHED`.
5. If anything fails, the **`ErrorRouter`** dispatches the exception to the appropriate handler.

---

## Key Concepts

| Concept | Description |
|---|---|
| **`Feeder`** | API entry point. Manages the publishing lifecycle (`startPublishing` / `stopPublishing`). |
| **`FeederStrategy`** | Strategy interface with two implementations: `ReactiveFeeder` and `PollingFeeder`. |
| **`ReactiveFeeder`** | Event-driven strategy — subscribes to `InboxItemStored` events on the bus. |
| **`PollingFeeder`** | Schedule-driven strategy — polls the inbox at a configurable interval (default: 30s). |
| **`InboxReader`** | Interface you implement to query inbox items by status (used by `PollingFeeder`). |
| **`ChannelRouter`** | Routes events to named channels based on event type. Default channel: `"spool"`. |
| **`FeederBuilderFactory`** | DSL entry point with `reactive()` and `polling()` presets. |
| **`ErrorRouter`** | Maps exception types to handlers. Default mapping provided by `FeederErrorRouter`. |

---

## Installation

### Maven (GitHub Packages)

```xml
<dependency>
    <groupId>software.spool</groupId>
    <artifactId>feeder</artifactId>
    <version>1.0.2-SNAPSHOT</version>
</dependency>
```

Configure the GitHub Packages repository in your `pom.xml` or `settings.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Spool-FRAMEWORK/packages</url>
    </repository>
</repositories>
```

> Requires **Java 17+**.

---

## Quick Start

```java
// 1. Create your infrastructure
InMemoryEventBus bus = new InMemoryEventBus();
InMemoryInbox inbox = new InMemoryInbox();

// 2. Create a reactive feeder
Feeder feeder = FeederBuilderFactory.reactive()
        .from(bus)                                         // listen for InboxItemStored events
        .with(inbox)                                       // update inbox statuses
        .on(bus)                                           // emit ItemPublished events
        .withErrorRouter(FeederErrorRouter.defaults(bus))  // default error handling
        .create();

// 3. Start and stop
feeder.startPublishing();
// ... items flow from inbox to event channels ...
feeder.stopPublishing();
```

---

## Examples

### Reactive Publishing

The feeder subscribes to `InboxItemStored` events on the event bus and processes each one immediately. Best for low-latency, event-driven architectures.

```java
Feeder feeder = FeederBuilderFactory.reactive()
        .from(eventBusListener)
        .with(inboxUpdater)
        .on(eventBusEmitter)
        .withErrorRouter(FeederErrorRouter.defaults(eventBusEmitter))
        .create();

feeder.startPublishing();
```

---

### Polling-based Publishing

The feeder periodically queries the inbox for items with `PUBLISHING` status. Best for batch-oriented or high-throughput scenarios.

```java
Feeder feeder = FeederBuilderFactory.polling()
        .from(inboxReader)
        .with(inboxUpdater)
        .on(eventBusEmitter)
        .withErrorRouter(FeederErrorRouter.defaults(eventBusEmitter))
        .create();

feeder.startPublishing();
```

---

### Custom Polling Interval

Override the default 30-second polling interval.

```java
Feeder feeder = FeederBuilderFactory.polling()
        .from(inboxReader)
        .with(inboxUpdater)
        .on(eventBusEmitter)
        .each(Duration.ofSeconds(5))    // poll every 5 seconds
        .withErrorRouter(FeederErrorRouter.defaults(eventBusEmitter))
        .create();

feeder.startPublishing();
```

---

### Channel Routing by Event Type

Use `ChannelRouter` to send events to different channels based on their type.

```java
ChannelRouter router = new ChannelRouter()
        .route(OrderReceived.class, "orders")
        .route(PaymentProcessed.class, "payments")
        .route(UserRegistered.class, "users");

// Resolve the channel for any event
String channel = router.resolve(myEvent);  // "orders", "payments", "users", or default "spool"
```

---

### Custom Error Routing

Override the default error routing to add logging, metrics, or recovery logic.

```java
ErrorRouter customRouter = new ErrorRouter()
        .on(InboxReadException.class, (e, cause) -> {
            logger.error("Failed to read inbox: {}", e.getMessage());
            eventBusEmitter.emit(InboxItemConsumptionFailed.builder()
                    .errorMessage(e.getMessage()).build());
        })
        .on(InboxUpdateException.class, (e, cause) -> {
            logger.error("Failed to update inbox: {}", e.getMessage());
            metrics.increment("inbox.update.failures");
        });

Feeder feeder = FeederBuilderFactory.reactive()
        .from(eventBusListener)
        .with(inboxUpdater)
        .on(eventBusEmitter)
        .withErrorRouter(customRouter)
        .create();

feeder.startPublishing();
```

---

### Full Pipeline (Crawler → Publisher → Ingester)

A complete example showing how all three Spool modules work together.

```java
// Shared infrastructure
InMemoryEventBus bus = new InMemoryEventBus();
InMemoryInbox inbox = new InMemoryInbox();

// 1. Crawler — fetches JSON from an API and stores records in the inbox
CrawlerStrategy crawler = CrawlerBuilderFactory.poll(new MyApiSource())
        .withFormat(Formats.JSON_ARRAY)
        .ports(CrawlerPorts.builder()
                .inbox(inbox)
                .bus(bus)
                .build())
        .create();

// 2. Publisher — reacts to InboxItemStored events and publishes them
Feeder feeder = FeederBuilderFactory.reactive()
        .from(bus)
        .with(inbox)
        .on(bus)
        .withErrorRouter(FeederErrorRouter.defaults(bus))
        .create();

// 3. Ingester — buffers ItemPublished events and writes to data lake
Ingester ingester = IngesterBuilderFactory.buffered()
        .from(bus)
        .storesWith(myDataLakeWriter)
        .updatedWith(inbox)
        .on(bus)
        .flushWith(FlushPolicy.whenReaches(50).orEvery(Duration.ofSeconds(10)))
        .create();

// Start the pipeline (order matters: downstream first)
ingester.startIngestion();
feeder.startPublishing();
crawler.execute();
```

---

## API Reference

### Builder DSL — Reactive

| Step | Method | Description |
|---|---|---|
| Entry point | `FeederBuilderFactory.reactive()` | Start building a reactive feeder. |
| Listener | `.from(eventBusListener)` | Set the event bus to subscribe to. |
| Updater | `.with(inboxUpdater)` | Set the inbox updater. |
| Emitter | `.on(eventBusEmitter)` | Set the event bus to emit events on. |
| Errors | `.withErrorRouter(errorRouter)` | Set the error router. |
| Create | `.create()` | Return the configured `Feeder`. |

### Builder DSL — Polling

| Step | Method | Description |
|---|---|---|
| Entry point | `FeederBuilderFactory.polling()` | Start building a polling feeder. |
| Reader | `.from(inboxReader)` | Set the inbox reader for querying items. |
| Updater | `.with(inboxUpdater)` | Set the inbox updater. |
| Emitter | `.on(eventBusEmitter)` | Set the event bus to emit events on. |
| Interval | `.each(Duration.ofSeconds(15))` | Set the polling interval (default: 30s). |
| Errors | `.withErrorRouter(errorRouter)` | Set the error router. |
| Create | `.create()` | Return the configured `Feeder`. |

### Feeder Lifecycle

| Method | Description |
|---|---|
| `startPublishing()` | Start the underlying strategy (subscribe or schedule). |
| `stopPublishing()` | Stop the strategy and release resources. |

---

## Event Model

| Event | When Emitted |
|---|---|
| `InboxItemStored` | _Consumed_ — triggers the publishing process (reactive mode). |
| `ItemPublished` | After an inbox item is read and its payload is published. |
| `InboxItemConsumptionFailed` | When `InboxReader.findByStatus()` throws an exception. |
| `InboxItemStoreFailed` | When `InboxUpdater.update()` throws an exception. |

### Inbox Item Status Lifecycle

```
STORED → PUBLISHING → PUBLISHED
```

| Status | Meaning |
|---|---|
| `PUBLISHING` | The item has been picked up and is being published. |
| `PUBLISHED` | The item has been successfully published to the event bus. |

---

## License

Distributed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
