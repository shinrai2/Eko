package cc.shinrai.eko;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Shinrai on 2017/5/4 0004.
 */

public class TcpServer {
    private static final String TAG         = "TcpServer";
    private static final int    BUFFER_SIZE = 65536;
    private Handler             disconnectHandler;

    public TcpServer(Handler handler) {
        this.disconnectHandler = handler;
    }

    /**
     * 通过tcp协议连接指定的设备并发送、接收信息和文件。
     * @param address 要进行连接的目的地址。
     * @param mediaPlayer 播放器的实例，在发送前获取currentPosition，以降低tcp协议建立的耗时导致的延时
     * @param musicPath 要发送的音乐文件的储存地址， null 代表不发送。
     * @param position 现在播放的音乐在音乐列表中的位置。
     */
    public void connect(final String address, final MediaPlayer mediaPlayer, final String musicPath, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(address, 9999);
                    OutputStream outStream = socket.getOutputStream();
                    //发送前再构造，尽可能减少延时 (\n 用于接收时判断字符串尾部)
                    String xhead = position + "-" + mediaPlayer.getCurrentPosition() +
                            "-" + new Date().getTime() + "-" + (musicPath!=null?"1":"0") + "\n";
                    outStream.write(xhead.getBytes());
                    outStream.flush();
                    Log.i(TAG, xhead);
                    //等待feedback
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String feedbackString = br.readLine();
                    //feedback后掐一个时间，计算一个发送周期时间长度。
                    long feedbackTime = new Date().getTime();
                    //处理反馈信息
                    Log.i(TAG, feedbackString);
                    //判断状态 & 等待发送文件
                    if(musicPath != null) {
                        InputStream fileinputStream = new FileInputStream(musicPath);
                        byte writebuffer[] = new byte[BUFFER_SIZE];
                        int len = 0;
                        while ((len = fileinputStream.read(writebuffer)) != -1) {
                            outStream.write(writebuffer, 0, len);
                        }
                        Log.i(TAG, "file sent.");
                        outStream.flush();
                        fileinputStream.close();
                    }
                    //get another feedback
                    String anotherFeedbackString = br.readLine();
                    //文件传输完成后，发送状态和半个周期  (用于缩小播放延时)

                    //ensure the stream is closed.
                    br.close();
                    outStream.close();
                    socket.close();
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
