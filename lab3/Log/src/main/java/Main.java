import java.util.Deque;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Storage storage = new Storage();
        Thread logStatsThread = new Thread(new LogStatsThread(storage));
        Thread logVisualConsumerThread = new Thread(new LogVisualConsumerThread(storage));
        ShowDeviceData showDeviceData = new ShowDeviceData(storage);

        logStatsThread.start();
        logVisualConsumerThread.start();

        String choice = "1";
        while(!"exit".equalsIgnoreCase(choice)) {
            Scanner sc = new Scanner(System.in);
            System.out.println("--------------------------------------------------");
            System.out.println("Enter device id: ");
            String deviceId = sc.nextLine();
            if(storage.checkDeviceId(deviceId)) {
                System.out.println("The log information of this device is as follows: ");
                showDeviceData.showData(deviceId);
                System.out.println("The number of severe alarms is " + storage.getCriticalAlertCount(deviceId) + ".");
                System.out.println("The line chart is as follows: ");
                Deque<AnalysisResult> result = storage.getAnalysisResults(deviceId);
                LogVisualDraw.draw(deviceId, result);
                System.out.println("Enter anything to continue and enter exit to quit");
                choice = sc.nextLine();
                if ("exit".equalsIgnoreCase(choice)) {
                    break;
                }
                System.out.println("--------------------------------------------------");
            }
            else {
                System.out.println("The device id does not exist.");
            }
        }
    }
}
