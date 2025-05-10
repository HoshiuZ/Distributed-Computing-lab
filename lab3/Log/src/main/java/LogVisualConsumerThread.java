import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class LogVisualConsumerThread implements Runnable{
    private final Storage storage;
    private volatile boolean running = true;

    public LogVisualConsumerThread(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        try{
            String brokerURL = "tcp://localhost:61616";
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
            Connection connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer1 = session.createConsumer(session.createQueue("analysisResults"));
            MessageConsumer consumer2 = session.createConsumer(session.createQueue("criticalAlerts"));

            AnalysisResultListener analysisResultListener = new AnalysisResultListener(storage);
            CriticalAlertListener criticalAlertListener = new CriticalAlertListener(storage);
            consumer1.setMessageListener(analysisResultListener);
            consumer2.setMessageListener(criticalAlertListener);

            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        running = false;
    }
}
