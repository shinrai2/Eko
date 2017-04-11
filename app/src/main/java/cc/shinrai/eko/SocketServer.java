package cc.shinrai.eko;

import android.app.Service;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Shinrai on 2017/3/27 0027.
 */

public class SocketServer {
    private static final String TAG = "SocketServer";
    private int             port;// 监听端口
    private final int       BUFFER_SIZE = 65536;
    private ExecutorService executorService;// 线程池
    private boolean         quit;// 是否退出
    private ServerSocket    ss = null;
    private String          path;
    private Handler         mHandler;

    public void setPath(String p) {
        path = p;
    }

    public void stopSocket() {
        quit = true;
    }

    public SocketServer(int port, Handler handler) {
        this.port = port;
        this.mHandler = handler;
        quit = false;
        // 初始化线程池
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * 50);
    }


    // 启动服务
    public void start() throws Exception {
        ss = new ServerSocket(port);
        while (!quit) {
            Socket socket = ss.accept();// 接受客户端的请求
            // 为支持多用户并发访问，采用线程池管理每一个用户的连接请求
            Log.i(TAG, "A device connect.");
            executorService.execute(new SocketTask(socket));// 启动一个线程来处理请求
        }
    }

    private class SocketTask implements Runnable {
        private Socket socket;

        public SocketTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if(path != null) {
                sendData(socket);
            }
        }
    }

    private void sendData(Socket socket) {
        try {
            InputStream fileinputStream = new FileInputStream(path);
            OutputStream outputStream = socket.getOutputStream();
            byte writebuffer[] = new byte[BUFFER_SIZE];
            int wtemp = 0;
            while ((wtemp = fileinputStream.read(writebuffer)) != -1) {
                // 把数据写入到OuputStream对象中
                outputStream.write(writebuffer, 0, wtemp);
            }
            // 发送读取的数据到服务端
            outputStream.flush();
            fileinputStream.close();
            outputStream.close();
//            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送UDP广播
        Message msg = new Message();
        msg.what = HostService.SEND_MESSAGE;
        mHandler.sendMessage(msg);
    }

}
