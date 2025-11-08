#!/bin/bash
# Setup script for interview coding practice environment

echo "🚀 Setting up Interview Coding Practice Environment"
echo "=================================================="

# Navigate to project directory
cd /home/messx/Projects/interview-coding-practice

# Use system Python (no venv needed as we only use standard library)
export PYTHON=/usr/bin/python3

# Add project to PYTHONPATH
export PYTHONPATH=/home/messx/Projects/interview-coding-practice:$PYTHONPATH

echo "✅ Environment ready!"
echo ""
echo "Python: $($PYTHON --version)"
echo "Project: $(pwd)"
echo ""
echo "Quick commands:"
echo "  Run log analyzer:    $PYTHON log-analysis/log_analyzer.py"
echo "  Run tests:           $PYTHON log-analysis/test_log_analyzer.py"
echo "  Interactive Python:  $PYTHON"
echo ""

# Start an interactive shell
exec $SHELL

