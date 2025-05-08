import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class LogStats {
    private static String brokerURL = "tcp://localhost:61616";
    private static ConnectionFactory factory;
    private static Connection connection;
    private Session session;
    private MessageProducer producer1, producer2;
    private MessageConsumer consumer;
    private LogStatsListener logStatsListener;

    public LogStats() throws JMSException {
        factory = new ActiveMQConnectionFactory(brokerURL);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer1 = session.createProducer(session.createQueue("analysisResults"));
        producer2 = session.createProducer(session.createQueue("criticalAlerts"));
        consumer = session.createConsumer(session.createQueue("logs"));
        logStatsListener = new LogStatsListener(session, producer1, producer2);
    }

    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) throws JMSException {
        LogStats logstats = new LogStats();
        try {
            logstats.consumer.setMessageListener(logstats.logStatsListener);

            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}
