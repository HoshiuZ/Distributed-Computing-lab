import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;

public class AnalysisResultListener implements MessageListener {
    private final Storage storage;

    public AnalysisResultListener(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void onMessage(Message message) {
        try{
            if(!(message instanceof TextMessage textMessage)) return;
            String msg = textMessage.getText();

            ObjectMapper mapper = new ObjectMapper();
            AnalysisResult analysisResult = mapper.readValue(msg, AnalysisResult.class);

            storage.addAnalysisResult(analysisResult.device_id, analysisResult);
        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
