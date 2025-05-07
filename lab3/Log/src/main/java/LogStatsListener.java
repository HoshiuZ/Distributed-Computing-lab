import jakarta.jms.*;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogStatsListener implements MessageListener {
    private static final int N = 20;
    private static final int T = 5;
    private static final int S = 10;

    private Map<String, Deque<LogEntry>> deviceLogs = new ConcurrentHashMap<>();
    private Session session;
    private MessageProducer statsProducer;
    private MessageProducer warnProducer;

    public LogStatsListener(Session session) throws JMSException {
        this.session = session;
        this.statsProducer = session.createProducer(session.createQueue("stats"));
        this.warnProducer = session.createProducer(session.createQueue("log"));


    }

}
