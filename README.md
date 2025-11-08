# Interview Coding Practice

This repository contains hands-on coding problems for technical interview preparation, organized by topic and difficulty level.

## 🎯 Purpose

Practice coding problems to prepare for Engineering Manager and Senior/Staff Engineer interviews at top-tier tech companies.

## 📁 Structure

```
interview-coding-practice/
├── log-analysis/                    # Log parsing and metrics generation (Python)
├── inventory-reservation-system/   # Inventory management with concurrency (Java/Spring Boot)
├── rate-limiter/                   # (Coming soon) Rate limiting algorithms
├── cache-design/                   # (Coming soon) Distributed cache implementation
├── event-processing/               # (Coming soon) Stream processing pipeline
└── README.md
```

## 🛠️ Environment Setup

Each problem directory contains:
- `README.md` - Problem statement and requirements
- Dependencies file (`requirements.txt` for Python, `build.gradle` for Java)
- Test files for validation
- Solution templates with TODOs

### Python Projects (log-analysis)

```bash
# Use system Python (no dependencies needed)
cd log-analysis
python3 log_analyzer.py

# Or set up environment
export PYTHON=/usr/bin/python3
```

### Java/Spring Boot Projects (inventory-reservation-system)

```bash
cd inventory-reservation-system

# Build
./gradlew build

# Run
./gradlew bootRun

# Test
./gradlew test

# Access H2 Console: http://localhost:8080/h2-console
```

## 📚 Problems

### 1. Log Analysis & Metrics Generation
**Language:** Python  
**Difficulty:** Medium  
**Topics:** String processing, data structures, file I/O, aggregation  
**Status:** ✅ Completed

Parse log files and generate metrics (request counts, response times, error rates).

### 2. Inventory Reservation System
**Language:** Java 17 + Spring Boot  
**Difficulty:** Hard  
**Topics:** Concurrency, transactions, locking, race conditions, ACID  
**Status:** 🚀 Ready to practice

Build an inventory management system that handles concurrent reservations, prevents overselling, and manages reservation expiration. Focus on pessimistic/optimistic locking, transaction isolation levels, and race condition handling.

**Key Features:**
- Reserve/release inventory with concurrency control
- Prevent overselling using database locks
- Automatic expiration cleanup
- Transaction management with Spring

**Setup:** See `inventory-reservation-system/README.md`

### 3. Rate Limiter (Coming Soon)
**Language:** Python/Go  
**Difficulty:** Medium  
**Topics:** Algorithms, concurrency, system design

Implement token bucket, sliding window, and leaky bucket algorithms.

### 3. Distributed Cache (Coming Soon)
**Language:** Python/Go  
**Difficulty:** Hard  
**Topics:** System design, data structures, eviction policies

Build LRU/LFU cache with TTL support.

### 4. Event Processing Pipeline (Coming Soon)
**Language:** Python  
**Difficulty:** Hard  
**Topics:** Stream processing, async I/O, data pipelines

Process events with filtering, transformation, and aggregation.

## 🎓 Learning Goals

- ✅ Practice problem-solving under time constraints
- ✅ Improve code quality and design patterns
- ✅ Build familiarity with common interview patterns
- ✅ Demonstrate hands-on technical depth

## 📈 Progress Tracking

Track your practice sessions in Notion:
- Problem attempted
- Time taken
- Key learnings
- Areas for improvement

---

**Last Updated:** November 8, 2025


