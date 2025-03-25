package lab1;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread{
    Socket socket = null;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        OutputStream os = null;
        PrintWriter pw = null;
        try{
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            os = socket.getOutputStream();
            pw = new PrintWriter(os, true);
            String line = null;
            while((line=br.readLine())!=null) {
                System.out.println("接收到来自客户端" + this.getName() + "的指令 : " + line);
                String[] parts = line.split("\\s+");
                String command = parts[0];
                if(command.equals("PUT")) {
                    if(parts.length != 3) {
                        pw.println("指令格式不正确。");
                        pw.flush();
                    }
                    else {
                        String key = parts[1];
                        String value = parts[2];
                        KeyValueServer.map.put(key, value);
                        pw.println("键值对(" + key + "," + value + ")存储成功！");
                        pw.flush();
                    }
                }
                else if(command.equals("GET")) {
                    if(parts.length != 2) {
                        pw.println("指令格式不正确。");
                        pw.flush();
                    }
                    else {
                        String key = parts[1];
                        String value = KeyValueServer.map.get(key);
                        if (value != null) {
                            pw.println("键" + key + "所对应的值为" + value + "。");
                            pw.flush();
                        } else {
                            pw.println("键" + key + "目前没有所对应的值。");
                            pw.flush();
                        }
                    }
                }
                else if(command.equals("DELETE")) {
                    if(parts.length != 2) {
                        pw.println("指令格式不正确。");
                        pw.flush();
                    }
                    else {
                        String key = parts[1];
                        if (KeyValueServer.map.containsKey(key)) {
                            String value = KeyValueServer.map.get(key);
                            KeyValueServer.map.remove(key);
                            pw.println("键值对(" + key + "," + value + ")删除成功！");
                            pw.flush();
                        } else {
                            pw.println("当前不存在以" + key + "为键的键值对。");
                            pw.flush();
                        }
                    }
                }
                else if(command.equals("EXIT")) {
                    if(parts.length != 1) {
                        pw.println("指令格式不正确。");
                        pw.flush();
                    }
                    else {
                        pw.println("正在关闭连接。");
                        pw.flush();
                        break;
                    }
                }
                else {
                    pw.println("不存在该指令。");
                    pw.flush();
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            try {
                if(pw!=null) pw.close();
                if(os!=null) os.close();
                if(br!=null) br.close();
                if(is!=null) is.close();
                if(isr!=null) isr.close();
                if(socket!=null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
