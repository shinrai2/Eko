package cc.shinrai.eko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends Service {
    private static final String TAG = "HostService";
//    private UdpServer udpServer = null;
    private MediaPlayer mediaPlayer =  new MediaPlayer();
    private boolean isPause;    //暂停状态
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.mp3";
    private boolean flag = false; //发送状态的标记

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public final IBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        HostService getService() {
            return HostService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

//    public void sendMessage() {
//        Log.i(TAG, "sendMessage");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "before send.");
//                while (flag) {
//                    udpServer.sendMessage(new SimpleDateFormat("hh:mm:ss")
//                            .format(new java.util.Date()));
//                    Log.i(TAG, "send.");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    public void play(int position) {
        try {
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();  //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    @Override
    public void onDestroy() {
        this.flag = false;
//        udpServer.closeSocket();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "start.");
//        if (udpServer == null)
//            udpServer = new UdpServer();
        super.onCreate();
    }

    private class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int positon;

        public PreparedListener(int p0) {
            positon = p0;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();    //开始播放
            if(positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
        }
    }
}
