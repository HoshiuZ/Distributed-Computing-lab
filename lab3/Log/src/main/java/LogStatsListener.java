import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogStatsListener implements MessageListener {
    private static final int N = 200;
    private static final int T = 5;
    private static final int S = 1;

    private Map<String, Deque<LogEntry>> deviceLogs = new ConcurrentHashMap<>();
    private Map<String, String> lastErrorTimestamp = new ConcurrentHashMap<>();
    private Map<String, Integer> errorCount = new ConcurrentHashMap<>();
    private Map<String, Integer> warnCount = new ConcurrentHashMap<>();
    private Map<String, Deque<LogEntry>> deviceLogsWithinSSeconds = new ConcurrentHashMap<>();
    private Map<String, Integer> errorCountWithinSSeconds = new ConcurrentHashMap<>();
    private Session session;
    private MessageProducer producer1, producer2;

    private static class TimedLogEntry{
        long timestamp;
        String logLevel;

        TimedLogEntry(long timestamp, String logLevel){
            this.timestamp = timestamp;
            this.logLevel = logLevel;
        }
    }

    private static class TimeUtil{
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public static long toTimestampMillis(String timeStr){
            LocalDateTime localDateTime = LocalDateTime.parse(timeStr, formatter);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }

    public LogStatsListener(Session session, MessageProducer producer1, MessageProducer producer2) throws JMSException {
        this.session = session;
        this.producer1 = session.createProducer(session.createQueue("analysisResults"));
        this.producer2 = session.createProducer(session.createQueue("criticalAlerts"));

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::analyzeAndSend, T, T, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(Message message) {
        try{
            if(!(message instanceof TextMessage textMessage)) return;
            String msg = textMessage.getText();

            ObjectMapper mapper = new ObjectMapper();
            LogEntry log = null;
            try {
                log = mapper.readValue(msg, LogEntry.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return;
            }

            String deviceId = log.device_id;
            deviceLogs.putIfAbsent(deviceId, new ConcurrentLinkedDeque<>());
            Deque<LogEntry> logs = deviceLogs.get(deviceId);

            if(logs.size() >= N) {
                LogEntry removedLog = logs.pollFirst();
                if("ERROR".equalsIgnoreCase(removedLog.log_level)) {
                    errorCount.put(deviceId, errorCount.get(deviceId) - 1);
                } else if("WARN".equalsIgnoreCase(removedLog.log_level)) {
                    warnCount.put(deviceId, warnCount.get(deviceId) - 1);
                }
            }
            logs.addLast(log);

            if("ERROR".equalsIgnoreCase(log.log_level)) {
                errorCount.put(deviceId, errorCount.getOrDefault(deviceId, 0) + 1);
                lastErrorTimestamp.put(deviceId, log.timestamp);
            } else if("WARN".equalsIgnoreCase(log.log_level)) {
                warnCount.put(deviceId, warnCount.getOrDefault(deviceId, 0) + 1);
            }

            deviceLogsWithinSSeconds.putIfAbsent(deviceId, new ConcurrentLinkedDeque<>());
            logs = deviceLogsWithinSSeconds.get(deviceId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            while(!logs.isEmpty()) {
                LogEntry firstLog = logs.peekFirst();
                LocalDateTime firstTime = LocalDateTime.parse(firstLog.timestamp, formatter);
                if(java.time.Duration.between(firstTime, now).getSeconds() > S) {
                    LogEntry removedLog = logs.pollFirst();
                    if("ERROR".equalsIgnoreCase(removedLog.log_level)) {
                        errorCountWithinSSeconds.put(deviceId, errorCountWithinSSeconds.get(deviceId) - 1);
                    }
                } else {
                    break;
                }
            }
            logs.addLast(log);
            if("ERROR".equalsIgnoreCase(log.log_level)) {
                errorCountWithinSSeconds.put(deviceId, errorCountWithinSSeconds.getOrDefault(deviceId, 0) + 1);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void analyzeAndSend() {
        long currentTime = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();

        for(String deviceId : deviceLogs.keySet()) {
            Deque<LogEntry> logs = deviceLogs.get(deviceId);
            int total = logs.size();
            int errors = errorCount.getOrDefault(deviceId, 0);
            int warns = warnCount.getOrDefault(deviceId, 0);

            double errorRatio = total > 0 ? (double) errors / total : 0.0;
            double warnRatio = total > 0 ? (double) warns / total : 0.0;
            String lastErrorTime = lastErrorTimestamp.getOrDefault(deviceId, "No error.");

            AnalysisResult analysisResult = new AnalysisResult(deviceId, errorRatio, warnRatio, lastErrorTime);

            try{
                String analysisJson = mapper.writeValueAsString(analysisResult);
                Message analysisMsg = session.createTextMessage(analysisJson);

                producer1.send(analysisMsg);
                System.out.println("Sent analysis result for device: " + deviceId);

                logs = deviceLogsWithinSSeconds.get(deviceId);
                int totalWithinSSeconds = logs.size();
                int errorsWithinSseconds = errorCountWithinSSeconds.get(deviceId);
                double errorRatioWithinSseconds = totalWithinSSeconds > 0 ? (double) errorsWithinSseconds / totalWithinSSeconds : 0.0;
                if(errorRatioWithinSseconds > 0.5) {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    CriticalAlert criticalAlert = new CriticalAlert(deviceId, timestamp, "ERROR ratio exceeds 50%!");
                    String alertJson = mapper.writeValueAsString(criticalAlert);
                    Message alertMsg = session.createTextMessage(alertJson);
                    producer2.send(alertMsg);
                    System.out.println("Sent alert result for device: " + deviceId);
                }

            } catch (JsonProcessingException | JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
