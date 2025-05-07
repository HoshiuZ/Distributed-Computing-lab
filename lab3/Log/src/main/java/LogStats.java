import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class LogStats {
    public static void main(String[] args) throws JMSException {
        String brokerURL = "tcp://localhost:61616";
        ConnectionFactory factory = null;
        Connection connection = null;
        Session session = null;
        MessageProducer destination = null;
        MessageConsumer messageConsumer = null;
        LogStatsListener listener = null;
    }
}
