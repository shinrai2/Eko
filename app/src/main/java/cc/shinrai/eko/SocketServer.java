package cc.shinrai.eko;

import android.os.Environment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private int port;// 监听端口
    private ExecutorService executorService;// 线程池
    private boolean quit;// 是否退出
    private ServerSocket ss = null;
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.mp3";

    public SocketServer(int port) {
        this.port = port;
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
            try {
                InputStream inputStream = new FileInputStream(path);
                OutputStream outputStream = socket.getOutputStream();
                byte buffer[] = new byte[4 * 1024];
                int temp = 0;
                while ((temp = inputStream.read(buffer)) != -1) {
                    // 把数据写入到OuputStream对象中
                    outputStream.write(buffer, 0, temp);
                }
                // 发送读取的数据到服务端
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
