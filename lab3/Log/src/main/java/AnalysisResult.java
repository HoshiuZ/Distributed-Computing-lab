public class AnalysisResult {
    public String device_id;
    public double errorRatio;
    public double warningRatio;
    public String lastErrorTime;

    public AnalysisResult(String device_id, double errorRatio, double warningRatio, String lastErrorTimeStamp) {
        this.device_id = device_id;
        this.errorRatio = errorRatio;
        this.warningRatio = warningRatio;
        this.lastErrorTime = lastErrorTime;
    }
}
