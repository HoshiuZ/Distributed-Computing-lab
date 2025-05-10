public class CriticalAlert {
    public String device_id;
    public String timestamp;
    public String msg;

    public CriticalAlert() {}
    public CriticalAlert(String device_id, String timestamp, String msg) {
        this.device_id = device_id;
        this.timestamp = timestamp;
        this.msg = msg;
    }
}
