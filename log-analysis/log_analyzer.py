#!/usr/bin/env python3
"""
Log Analysis and Metrics Generation
Interview Problem: Build a monitoring system for web application logs

Author: Interview Practice Session
Date: 2025-01-17
Log format: 
2024-01-17T10:30:15Z INFO payment_service Payment processed successfully for user_id=12345

"""

from datetime import datetime
import os
from log import Log

service_log_count = {}
service_log_error_count = {}
error_msg_count = {}
error_time_distribution = {}

def analyze_logs(logs):
    """
    Analyze the logs and generate metrics
    """
    pass

def parse_log(log_line): 
    """
    Parse the log line and return a Log object
    """
    log_input_arr = log_line.split(' ')
    if len(log_input_arr) < 4:
        return None  # Invalid log format
    timestamp = datetime.strptime(log_input_arr[0], "%Y-%m-%dT%H:%M:%SZ")
    log_level = log_input_arr[1]
    service_name = log_input_arr[2]
    message = ' '.join(log_input_arr[3:])
    return Log(timestamp, log_level, service_name, message)

    



def calculate_service_log_and_error_count(logs):
    """
    Calculate the number of log and error logs for each service
    """
    for log in logs:
        
        

        if log.service not in service_log_count:
            service_log_count[log.service] = 0
        service_log_count[log.service] += 1
        
        if log.log_level == "ERROR":
            if log.message not in error_msg_count:
                error_msg_count[log.message] = 0
            error_msg_count[log.message] += 1

            if log.service not in service_log_error_count:
                service_log_error_count[log.service] = 0
            service_log_error_count[log.service] += 1


def main():
    """
    Main function to demonstrate log analysis
    """
    print("🚀 Starting Log Analysis Session")
    print("=" * 50)
    current_dir = os.getcwd()
    with open(current_dir + "/log-analysis/sample_logs.txt", "r") as file:
        logs = [parse_log(line.strip()) for line in file if parse_log(line.strip())]
    calculate_service_log_and_error_count(logs)
    print("Service Log Count:", service_log_count)
    print("Service Error Log Count:", service_log_error_count)
    for service, count in service_log_count.items():
        error_count = service_log_error_count.get(service, 0)
        error_rate = (error_count / count) * 100 if count > 0 else 0
        print(f"Service: {service}, Error Rate: {error_rate:.2f}%")
    max_error_msg = ""
    max_error_count = 0
    for error_msg, count in error_msg_count.items():
        if count > max_error_count:
            max_error_count = count
            max_error_msg = error_msg

        print(f"Error Message: '{error_msg}' occurred {count} times")
    
    print("=" * 50)
    print(f"Most Frequent Error Message: '{max_error_msg}' occurred {max_error_count} times")
if __name__ == "__main__":
    main()
   