package cc.shinrai.eko;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

/**
 * Created by Shinrai on 2017/5/4 0004.
 */

public class TcpServer {

    public void connect(final String address, final String head, final String musicPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(address, 9999);
                    OutputStream outStream = socket.getOutputStream();
                    //发送信息头
                    outStream.write(head.getBytes());
                    //等待feedback
                    InputStream in = socket.getInputStream();
                    DataInputStream din = new DataInputStream(in);
                    String feedbackString = new String(din.readUTF());
                    //判断状态 & 等待发送文件
                    if(musicPath != null) {

                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    //出错，在 list 中删除 address

                }
            }
        }).start();
    }

}
