public class CriticalAlert {
    public String device_id;
    public String timeStamp;
    public String msg;

    public CriticalAlert(String device_id, String timeStamp, String msg) {
        this.device_id = device_id;
        this.timeStamp = timeStamp;
        this.msg = msg;
    }
}
