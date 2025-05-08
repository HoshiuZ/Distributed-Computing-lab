import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Publisher {
    private static String brokerURL = "tcp://localhost:61616";
    private static ConnectionFactory factory;
    private static Connection connection;
    private Session session;
    private MessageProducer producer;
    private String deviceid;

    public Publisher() throws JMSException {
        factory = new ActiveMQConnectionFactory(brokerURL);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(null);
        deviceid = null;
    }

    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }

    public void sendMessage() throws JMSException {
        Destination destination = session.createQueue("logs");
        ObjectMapper mapper = new ObjectMapper();
        Random random = new Random();
        String[] levels = {"INFO", "WARN", "ERROR"};
        while(true) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String level = levels[random.nextInt(levels.length)];
            String message = switch (level) {
                case "WARN" -> "警告";
                case "ERROR" -> "错误";
                default -> "正常";
            };

            LogEntry logentry = new LogEntry(deviceid, timestamp, level, message);
            String json = "";
            try {
                json = mapper.writeValueAsString(logentry);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Message msg = session.createTextMessage(json);
            producer.send(destination, msg);
            System.out.println("Sent message: " + json);

            try{
                Thread.sleep(100);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws JMSException {
        if(args.length == 0) {
            System.out.println("请提供 device_id 作为程序参数。");
            return;
        }
        Publisher publisher = new Publisher();
        publisher.deviceid = args[0];
        publisher.sendMessage();
        publisher.close();
    }
}
