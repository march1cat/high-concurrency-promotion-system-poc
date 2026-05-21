# High Concurrency Promotion System POC

A production-style microservices proof-of-concept focused on high-concurrency promotion inventory management, distributed caching, asynchronous persistence, and eventual consistency.

This project demonstrates how modern e-commerce systems handle:

- Flash sale inventory reservation
- Distributed cache protection
- Idempotent requests
- Read/write splitting
- Eventual consistency
- Distributed locking
- Gateway authentication
- Service-to-service communication

---

# Architecture Overview

```text
                           +-------------------+
                           |      Client       |
                           +---------+---------+
                                     |
                                     v
                    +--------------------------------+
                    |        Gateway Service         |
                    |--------------------------------|
                    | - JWT Authentication           |
                    | - Request ID Injection         |
                    | - Rate Limiting                |
                    | - Dynamic Routing              |
                    +---------------+----------------+
                                    |
                +-------------------+-------------------+
                |                                       |
                v                                       v
+--------------------------------+     +--------------------------------+
|        Product Service         |     |         Promo Service          |
|--------------------------------|     |--------------------------------|
| - Product Query                |     | - Promotion Inventory          |
| - Read/Write Splitting         |     | - Redis Lua Atomic Lock        |
| - Feign Client                 |     | - Async DB Persistence         |
| - Local Cache                  |     | - Distributed Lock             |
| - Circuit Breaker              |     | - Cache Rebuild                |
+---------------+----------------+     | - DeferredResult Waiting Pool  |
                |                      | - Idempotency                  |
                |                      +---------------+----------------+
                |                                      |
                v                                      v
     +---------------------+              +----------------------+
     |   MariaDB Primary   |              |        Redis         |
     +---------------------+              +----------------------+
     |   MariaDB Replica   |
     +---------------------+

                    +----------------------+
                    |        Nacos         |
                    |----------------------|
                    | - Config Center      |
                    | - Service Discovery  |
                    +----------------------+
```

---

# Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Cloud Gateway
- Spring Data JPA
- Spring AOP
- Spring Cache
- OpenFeign

---

## Infrastructure

- Redis
- MariaDB
- Nacos

---

## Cache

- Redis
- Caffeine

---

## Concurrency / Async

- RxJava3
- DeferredResult
- ThreadPoolTaskExecutor

---

## Security

- JWT

---

# Core Features

---

# 1. API Gateway

Gateway service acts as the unified entry point.

## Features

- JWT authentication
- Request tracing
- Dynamic routing
- Rate limiting
- Header propagation

## Implementations

### JWT Authentication

```java
AuthGlobalFilter
```

Responsibilities:

- Validate JWT token
- Extract user identity
- Inject X-User-Id header

---

### Request Tracing

```java
RequestHeaderFilter
```

Responsibilities:

- Generate request UUID
- Propagate X-Request-Id

---

### Rate Limiting

Implemented using:

```yaml
RequestRateLimiter
```

Backed by Redis token bucket algorithm.

---

### Dynamic Gateway Routing

Implemented using:

```java
RoutingRuleListener
```

Supports:

- Runtime route refresh
- Nacos dynamic configuration updates

---

# 2. Product Service

Handles product aggregation and promotion queries.

---

# Read / Write Splitting

Implemented using:

```java
AbstractRoutingDataSource
```

## Features

- Primary database for writes
- Replica database for reads
- AOP-based datasource routing

---

## Routing Annotations

### Read Replica

```java
@DBReadOnly
```

### Primary Database

```java
@UsePrimaryDatabase
```

---

# Feign Communication

Uses OpenFeign for service-to-service communication.

## Features

- Declarative HTTP client
- Fallback handling
- Header propagation

---

# Circuit Breaker

Implemented using:

```java
PromoClientFallbackFactory
```

Provides:

- Graceful degradation
- Service resilience

---

# Local Cache

Uses:

```java
@Cacheable
```

with:

- Caffeine
- Expiration policy
- Empty-result protection

---

# 3. Promo Service

Core service responsible for promotion inventory management.

---

# Inventory Reservation Flow

```text
Client Request
      |
      v
Redis Lua Atomic Decrease
      |
      v
Queue Async DB Update
      |
      v
Database Freeze Inventory
      |
      +---- success ---> Complete
      |
      +---- failure ---> Redis Rollback
```

---

# Redis Lua Atomic Inventory

Inventory operations are fully atomic.

## Supported Operations

### Occupy Inventory

```lua
inventory_occupy.lua
```

### Release Inventory

```lua
inventory_release.lua
```

---

# Distributed Lock

Implemented using Redis Lua scripts.

## Lock Operations

- Acquire lock
- Release lock

Ensures:

- Cache rebuild protection
- Duplicate request prevention

---

# Cache Protection Strategy

This project implements multiple anti-breakdown strategies.

---

## Multi-Level Cache

### Level 1

Caffeine local cache

### Level 2

Redis distributed cache

---

## Logical Expiration

Cached objects contain:

```java
expireTime
```

Expired data can still be served while background refresh occurs.

---

## Cache Rebuild

When cache expires:

- One node acquires distributed lock
- Rebuilds cache asynchronously
- Publishes refresh notification

---

## Pub/Sub Notification

Redis Pub/Sub channel:

```text
on_complete_refresh_current_promoted
```

Used for:

- Cache synchronization
- Waiting request wake-up

---

# DeferredResult Waiting Pool

When cache rebuild is in progress:

- Requests do not hammer DB
- Requests wait asynchronously
- Pub/Sub wakes waiting requests

Implemented using:

```java
DeferredResult
ConcurrentHashMap
```

---

# Idempotency

Implemented using:

```java
@Idempotent
```

backed by:

- Spring AOP
- Redis distributed lock

---

# Purpose

Prevent:

- Duplicate order requests
- Repeated inventory reservation
- Retry duplication

---

# Async Database Persistence

Inventory update process:

1. Redis inventory deducted immediately
2. Push async task into queue
3. Background worker persists DB
4. Rollback Redis if persistence fails

---

# Queue Processing

Implemented using:

- RxJava3
- BlockingQueue
- Background executor pool

---

# Graceful Shutdown

QueueProcessor supports:

- Wait for queue drain
- Rollback remaining tasks
- Safe resource cleanup

---

# Eventual Consistency Design

This project intentionally prioritizes:

- High throughput
- Low latency
- Inventory protection

over strong consistency.

Final consistency is achieved asynchronously.

---

# Database Schema

## Product Service

### Products

| Column | Description |
|---|---|
| Id | Product ID |
| Name | Product Name |
| Description | Product Description |

---

## Promo Service

### PromotedItems

| Column | Description |
|---|---|
| Id | Promotion ID |
| ProductId | Product ID |
| Quantity | Total Quantity |
| IsSoldOut | Sold Out Status |

---

### PromotedInventory

| Column | Description |
|---|---|
| Id | Inventory ID |
| PromotedItemId | Promotion ID |
| Quantity | Total Quantity |
| FreezeQuantity | Reserved Quantity |

---

### OccupiedPromoteItems

| Column | Description |
|---|---|
| Id | Occupation Record ID |
| UserId | User ID |
| Quantity | Reserved Quantity |
| IsCanceled | Cancel Status |

---

# Distributed Cache Flow

```text
Request
   |
   v
Caffeine Cache
   |
   +---- hit ---> return
   |
   +---- miss
             |
             v
         Redis Cache
             |
             +---- hit ---> return
             |
             +---- miss
                       |
                       v
              Distributed Lock
                       |
                       +---- lock acquired
                       |         |
                       |         v
                       |   Rebuild Cache
                       |
                       +---- lock failed
                                 |
                                 v
                          Deferred Wait
```

---

# High Concurrency Design Highlights

## Features Demonstrated

- Distributed locking
- Atomic Redis inventory
- Cache collapse protection
- Logical expiration
- Eventual consistency
- Read/write splitting
- Async persistence
- Idempotency
- Gateway security
- Service resilience

---

# How To Run

---

# Required Infrastructure

- Redis
- MariaDB
- Nacos

---

# Recommended Startup Order

1. Nacos
2. Redis
3. MariaDB
4. Promo Service
5. Product Service
6. Gateway Service

---

# Example API Flow

---

## 1. Login

```http
POST /auth/login
```

Response:

```json
{
  "token": "jwt-token"
}
```

---

## 2. Create Promotion

```http
GET /api/products/newPromotion
```

---

## 3. Query Promotions

```http
GET /api/products/promoted
```

---

## 4. Occupy Inventory

```http
POST /api/promotions/occupyPromotion
```

Body:

```json
{
  "promotedItemId": "id",
  "quantity": 1
}
```

---

# Performance-Oriented Design Decisions

| Problem | Solution |
|---|---|
| Cache Breakdown | Distributed Lock |
| Cache Stampede | DeferredResult |
| Inventory Oversell | Redis Lua |
| Duplicate Requests | Idempotency |
| DB Pressure | Async Queue |
| Slow Cache Refresh | Logical Expiration |
| Service Failure | Circuit Breaker |
| Read Bottleneck | Read Replica |

---

# Future Improvements

## Planned Enhancements

- Docker Compose
- Kubernetes deployment
- OpenTelemetry tracing
- Prometheus metrics
- Grafana dashboard
- Kafka event streaming
- Distributed transaction orchestration
- Stress testing reports

---

# Learning Goals

This project was built to practice:

- Distributed system design
- High-concurrency architecture
- Cache consistency strategies
- Eventual consistency
- Spring Cloud ecosystem
- Production-style backend patterns

---

# Repository Structure

```text
gateway-service/
product-service/
promo-service/
```

---

# Author

Backend Engineer focused on:

- Java backend systems
- Distributed architecture
- High concurrency
- Event-driven systems
- Cache optimization
- System resilience