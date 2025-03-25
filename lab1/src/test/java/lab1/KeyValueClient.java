package lab1;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class KeyValueClient {
    public static void main(String[] args) throws Exception {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        try(Socket socket = new Socket("127.0.0.1", 1234)) {
            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
            PrintWriter out = new PrintWriter(outStream, true);
            Scanner sc = new Scanner(System.in);

            System.out.println("已连接到服务端，请输入指令：");
            while(true) {
                String command = sc.nextLine();
                out.println(command);
                if(command.equals("EXIT")) {
                    System.out.println("已关闭连接。");
                    break;
                }
                String response = in.readLine();
                if(response!=null) {
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
