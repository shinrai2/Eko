package cc.shinrai.eko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends Service {
    private static final String TAG = "HostService";
    private SocketServer mSocketServer;
    private UdpServer udpServer;
    private MediaPlayer mediaPlayer =  new MediaPlayer();
    private boolean flag = true; //udp标记
    private MusicInfo mMusicInfo;
    private Bitmap mBitmap;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public MusicInfo getMusicInfo() {
        return mMusicInfo;
    }

//    public boolean isFlag() {
//        return flag;
//    }
//
//    public void setFlag(boolean flag) {
//        this.flag = flag;
//    }

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

    public void sendMessage() {
        Log.i(TAG, "sendMessage");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "before send.");
                while (true) {
                    udpServer.sendMessage(((Boolean)mediaPlayer.isPlaying()).toString() + "-" +
                            mediaPlayer.getCurrentPosition() + "-" + new Date().getTime());
                    Log.i(TAG, "send.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void play(boolean play) {
        if(play) {
            mediaPlayer.start();
        }
        else {
            mediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    @Override
    public void onDestroy() {
        this.flag = false;
        udpServer.closeSocket();
        mSocketServer.stopSocket();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "start.");
        onSocketStart();
        super.onCreate();
    }

    public void onSocketStart() {
        //udp启动
        udpServer = new UdpServer();
        sendMessage();
        //tcp启动
        mSocketServer = new SocketServer(9999);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocketServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void prepare(MusicInfo musicInfo) {
        //musicInfo 和 mMusicInfo 位置不能调换
        if(musicInfo.equals_(mMusicInfo) == false) {
            mMusicInfo = musicInfo;
            mSocketServer.setPath(mMusicInfo.getPath());
            //设置发送文件的路径
            try {
                mediaPlayer.reset();//把各项参数恢复到初始状态
                mediaPlayer.setDataSource(musicInfo.getPath());
                mediaPlayer.prepare();  //进行缓冲
                flag = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
            fmmr.setDataSource(mMusicInfo.getPath());
            byte[] cover_data = fmmr.getEmbeddedPicture();
            if(cover_data != null) {
                mBitmap = BitmapFactory.decodeByteArray(cover_data, 0, cover_data.length);
            }
            else {
                mBitmap = null;
            }
        }
    }
}
