class Log:
    def __init__(self, timestamp, log_level, service, message):
        self.timestamp = timestamp
        self.log_level = log_level
        self.service = service
        self.message = message

    def __str__(self):
        return f"{self.timestamp} {self.log_level} {self.service} {self.message}"
    
    def __repr__(self):
        return f"Log(timestamp={self.timestamp}, log_level={self.log_level}, service={self.service}, message={self.message})"