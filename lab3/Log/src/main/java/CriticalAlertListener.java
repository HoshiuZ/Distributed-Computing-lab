import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;

public class CriticalAlertListener implements MessageListener {
    private final Storage storage;

    public CriticalAlertListener(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void onMessage(Message message) {
        try{
            if(!(message instanceof TextMessage textMessage)) return;
            String msg = textMessage.getText();

            ObjectMapper mapper = new ObjectMapper();
            CriticalAlert criticalAlert = mapper.readValue(msg, CriticalAlert.class);

            storage.updateCriticalAlert(criticalAlert.device_id);

        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
