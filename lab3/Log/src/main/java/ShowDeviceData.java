import java.util.Deque;

public class ShowDeviceData {
    private final Storage storage;

    public ShowDeviceData(Storage storage) {
        this.storage = storage;
    }

    public void showData(String deviceId) {
        Deque<AnalysisResult> analysisResults = storage.getAnalysisResults(deviceId);
        if(!analysisResults.isEmpty()) {
            AnalysisResult latestResult = analysisResults.peekLast();
            System.out.printf("Now the ratio of error is %.2f%%.%n", latestResult.errorRatio * 100);
            System.out.printf("Now the ratio of warn is %.2f%%.%n", latestResult.warnRatio * 100);
            System.out.printf("The latest error timestamp is %s.%n", storage.getLastErrorTimestamp(deviceId));
        }
    }
}
