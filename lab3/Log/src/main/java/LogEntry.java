public class LogEntry {
    public String device_id;
    public String timestamp;
    public String log_level;
    public String message;

    public LogEntry(String device_id, String timestamp, String log_level, String message) {
        this.device_id = device_id;
        this.timestamp = timestamp;
        this.log_level = log_level;
        this.message = message;
    }
}
