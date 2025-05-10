import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Storage {
    private final Map<String, Deque<AnalysisResult>> analysisResultsQueue = new ConcurrentHashMap<>();
    private final Map<String, String> lastErrorTimestamp = new ConcurrentHashMap<>();
    private final Map<String, Boolean> deviceIdFlag = new ConcurrentHashMap<>();
    private final Map<String, Integer> criticalAlertCount = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 20;

    public void addAnalysisResult(String deviceId, AnalysisResult analysisResult) {
        deviceIdFlag.put(deviceId, true);
        analysisResultsQueue.compute(deviceId, (k, deque) -> {
            if(deque == null) {
                deque = new ConcurrentLinkedDeque<>();
            }
            deque.addLast(analysisResult);
            if(deque.size() > MAX_HISTORY_SIZE) {
                deque.removeFirst();
            }
            return deque;
        });
    }

    public boolean checkDeviceId(String deviceId) {
        return deviceIdFlag.containsKey(deviceId);
    }

    public void updateCriticalAlert(String deviceId) {
        criticalAlertCount.put(deviceId, criticalAlertCount.getOrDefault(deviceId, 0) + 1);
    }

    public int getCriticalAlertCount(String deviceId) {
        return criticalAlertCount.getOrDefault(deviceId, 0);
    }

    public Deque<AnalysisResult> getAnalysisResults(String deviceId) {
        return analysisResultsQueue.getOrDefault(deviceId, new ConcurrentLinkedDeque<>());
    }

    public void updateLastErrorTimestamp(String deviceId, String timestamp) {
        lastErrorTimestamp.put(deviceId, timestamp);
    }

    public String getLastErrorTimestamp(String deviceId) {
        return lastErrorTimestamp.getOrDefault(deviceId, "No error.");
    }

}
