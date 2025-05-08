import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogStatsListener implements MessageListener {
    private static final int N = 20;
    private static final int T = 5;
    private static final int S = 10;

    private Map<String, Deque<LogEntry>> deviceLogs = new ConcurrentHashMap<>();
    private Map<String, String> lastErrorTimeStamp = new ConcurrentHashMap<>();
    private Session session;
    private MessageProducer producer1, producer2;

    public LogStatsListener(Session session, MessageProducer producer1, MessageProducer Producer2) throws JMSException {
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

            deviceLogs.putIfAbsent(deviceId, new ArrayDeque<>());
            Deque<LogEntry> logs = deviceLogs.get(deviceId);

            synchronized(logs) {
                if(logs.size() >= N) {
                    logs.pollFirst();
                }
                logs.addLast(log);
            }
            if("ERROR".equalsIgnoreCase(log.log_level)) {
                lastErrorTimeStamp.put(deviceId, log.timestamp);
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
            int errors = 0;
            int warnings = 0;
            String lastErrorTime = lastErrorTimeStamp.getOrDefault(deviceId, "No error.");
            synchronized(logs) {
                for(LogEntry log : logs) {
                    if("ERROR".equalsIgnoreCase(log.log_level)) errors++;
                    else if("WARNING".equalsIgnoreCase(log.log_level)) warnings++;
                }
            }

            double errorRatio = total > 0 ? (double) errors / total : 0.0;
            double warningRatio = total > 0 ? (double) warnings / total : 0.0;

            AnalysisResult analysisResult = new AnalysisResult(deviceId, errorRatio, warningRatio, lastErrorTime);

            try{
                String analysisJson = mapper.writeValueAsString(analysisResult);
                Message analysisMsg = session.createTextMessage(analysisJson);

                producer1.send(analysisMsg);
                System.out.println("Sent analysis result for device: " + deviceId);

                if(errorRatio > 0.5) {
                    String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    CriticalAlert criticalAlert = new CriticalAlert(deviceId, timeStamp, "ERROR ratio exceeds 50%!");
                    String alertJson = mapper.writeValueAsString(criticalAlert);
                    Message alertMsg = session.createTextMessage(alertJson);
                    producer2.send(alertMsg);
                    System.out.println("Sent critical alert for device: " + deviceId);
                }
            } catch (JsonProcessingException | JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
