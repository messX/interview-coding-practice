# Log Analysis and Metrics Generation

## Problem Statement
Build a monitoring system for web application logs. Parse server log entries and generate useful metrics for engineering teams.

## Input Format
```
timestamp level service_name message
```

## Requirements
1. Parse log entries and extract structured data
2. Generate metrics:
   - Total count of logs per service
   - Error rate per service (ERROR logs / total logs)
   - Most frequent error messages
   - Timeline of errors (hourly breakdown)

## Sample Data
See `sample_logs.txt` for test data.

## Usage
```bash
python log_analyzer.py
```

## Interview Focus
- Problem decomposition and approach
- Data structure selection
- Code organization and readability
- Error handling and edge cases
- Time/space complexity analysis
