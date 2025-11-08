#!/bin/bash

# Inventory Reservation System - Local Setup Script
# This script sets up and runs the application locally

set -e  # Exit on error

echo "🚀 Inventory Reservation System - Setup Script"
echo "================================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo -e "${YELLOW}1. Checking Java version...${NC}"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    echo "   ✅ Java version: $(java -version 2>&1 | head -n 1)"
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "   ⚠️  Warning: Java 17+ is recommended"
    fi
else
    echo "   ❌ Java not found. Please install Java 17+"
    exit 1
fi

echo ""
echo -e "${YELLOW}2. Building the project...${NC}"
./gradlew clean build --no-daemon

if [ $? -eq 0 ]; then
    echo "   ✅ Build successful"
else
    echo "   ❌ Build failed"
    exit 1
fi

echo ""
echo -e "${YELLOW}3. Running tests...${NC}"
./gradlew test --no-daemon

if [ $? -eq 0 ]; then
    echo "   ✅ All tests passed"
else
    echo "   ⚠️  Some tests failed. Check logs for details."
fi

echo ""
echo -e "${GREEN}✅ Setup complete!${NC}"
echo ""
echo "To start the application:"
echo "  ./gradlew bootRun"
echo ""
echo "Or run in background:"
echo "  ./gradlew bootRun &"
echo ""
echo "Application will be available at:"
echo "  📍 API: http://localhost:8080/api/inventory/status"
echo "  🗄️  H2 Console: http://localhost:8080/h2-console"
echo "     JDBC URL: jdbc:h2:mem:inventorydb"
echo "     Username: sa"
echo "     Password: (leave empty)"
echo ""
echo "To stop background process:"
echo "  pkill -f 'gradle.*bootRun'"
echo ""

