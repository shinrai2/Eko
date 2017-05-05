package cc.shinrai.eko;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Shinrai on 2017/5/4 0004.
 */

public class TcpServer {
    private static final String TAG = "TcpServer";
    private static final int    BUFFER_SIZE = 65536;
    private Handler             disconnectHandler;

    public TcpServer(Handler handler) {
        this.disconnectHandler = handler;
    }

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
                    InputStream inStream = socket.getInputStream();
                    DataInputStream dataInStream = new DataInputStream(inStream);
                    String feedbackString = new String(dataInStream.readUTF());
                    //处理反馈信息
                    Log.i(TAG, feedbackString);
                    //判断状态 & 等待发送文件
                    if(musicPath != null) {
                        InputStream fileinputStream = new FileInputStream(musicPath);
                        byte writebuffer[] = new byte[BUFFER_SIZE];
                        int wtemp = 0;
                        while ((wtemp = fileinputStream.read(writebuffer)) != -1) {
                            outStream.write(writebuffer, 0, wtemp);
                        }
                        outStream.flush();
                        fileinputStream.close();
                    }
                    //ensure the stream is closed.
                    inStream.close();
                    outStream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "Connect error : " + address);
                    //出错，在 list 中删除 address
                    Message msg = new Message();
                    msg.what = HostService.DISCONNECT_USER;
                    msg.obj = address;
                    disconnectHandler.sendMessage(msg);
                }
            }
        }).start();
    }

}
