public class AnalysisResult {
    public String device_id;
    public double errorRatio;
    public double warnRatio;
    public String lastErrorTime;

    public AnalysisResult() {}

    public AnalysisResult(String device_id, double errorRatio, double warnRatio, String lastErrorTime) {
        this.device_id = device_id;
        this.errorRatio = errorRatio;
        this.warnRatio = warnRatio;
        this.lastErrorTime = lastErrorTime;
    }
}
