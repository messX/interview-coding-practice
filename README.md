# Interview Coding Practice

This repository contains hands-on coding problems for technical interview preparation, organized by topic and difficulty level.

## 🎯 Purpose

Practice coding problems to prepare for Engineering Manager and Senior/Staff Engineer interviews at top-tier tech companies.

## 📁 Structure

```
interview-coding-practice/
├── log-analysis/           # Log parsing and metrics generation
├── rate-limiter/          # (Coming soon) Rate limiting algorithms
├── cache-design/          # (Coming soon) Distributed cache implementation
├── event-processing/      # (Coming soon) Stream processing pipeline
└── README.md
```

## 🐍 Environment Setup

Each problem directory contains:
- `README.md` - Problem statement and requirements
- `requirements.txt` - Python dependencies
- Test files for validation
- Solution templates

### Virtual Environment

```bash
# Create virtual environment (if not exists)
python3 -m venv coding-env

# Activate virtual environment
source coding-env/bin/activate

# Install dependencies for a specific problem
cd <problem-directory>
pip install -r requirements.txt
```

## 📚 Problems

### 1. Log Analysis & Metrics Generation
**Language:** Python  
**Difficulty:** Medium  
**Topics:** String processing, data structures, file I/O, aggregation  
**Status:** Ready to practice

Parse log files and generate metrics (request counts, response times, error rates).

### 2. Rate Limiter (Coming Soon)
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

**Last Updated:** November 2, 2025

