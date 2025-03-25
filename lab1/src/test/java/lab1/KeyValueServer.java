package lab1;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("InfiniteLoopStatement")
public class KeyValueServer {
    static Map<String, String> map = new ConcurrentHashMap<>();
    public static void main(String[] args) throws Exception {
        System.out.println("在端口1234监听。");

        try (ServerSocket listenSocket = new ServerSocket(1234)) {
            while (true) {
                try {
                    Socket socket = listenSocket.accept();
                    ServerThread serverThread = new ServerThread(socket);
                    serverThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
