package cc.shinrai.eko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends Service {
    public static final int SEND_MESSAGE = 9;
    public static final String UIREFRESH_PRIVATE = "cc.shinrai.eko.UIREFRESH_PRIVATE";
    private static final String TAG = "HostService";
    private SocketServer mSocketServer;
    private UdpServer udpServer;
    private MediaPlayer mediaPlayer =  new MediaPlayer();
    private MusicInfo mMusicInfo;
    private Bitmap mBitmap;
    private Handler mHandler;//在tcp发送歌曲完毕后的handler

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public MusicInfo getMusicInfo() {
        return mMusicInfo;
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

    public void sendMessage() {
        Log.i(TAG, "sendMessage");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "before send.");
                String tmpmsg = mMusicInfo.getMusicName() + "-" + mMusicInfo.getDurationTime() +
                        "-" + mediaPlayer.isPlaying() + "-" + mediaPlayer.getCurrentPosition() +
                        "-" + new Date().getTime();

                udpServer.sendMessage(tmpmsg);
                Log.i(TAG, tmpmsg + " had sent.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void play(boolean isPlay) {
        if(isPlay) {
            mediaPlayer.start();
            sendMessage();
        }
        else {
            mediaPlayer.pause();
            sendMessage();
        }
    }

    //获取播放状态 TRUE = 播放 FALSE = 暂停
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    //获取目前播放的位置
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    };

    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    @Override
    public void onDestroy() {
        udpServer.closeSocket();
        mSocketServer.stopSocket();
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "start.");
        setListener();
        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SEND_MESSAGE:
                        HostService.this.sendMessage();
                        break;
                    default:
                        break;
                }
            }
        };
        onSocketStart();
        super.onCreate();
    }

    public void onSocketStart() {
        //udp启动
        udpServer = new UdpServer();
//        sendMessage();
        //tcp启动
        mSocketServer = new SocketServer(9999, mHandler);
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

    //缓冲音乐并开始播放
    public void prepare(MusicInfo musicInfo) {
        if(musicInfo.equals_(mMusicInfo) == false) {
            mMusicInfo = musicInfo;
            mSocketServer.setPath(mMusicInfo.getPath());
            //设置发送文件的路径
            if(mediaPlayer.isPlaying() == true) {
                play(false);
            }
            try {
                mediaPlayer.reset();//把各项参数恢复到初始状态
                mediaPlayer.setDataSource(musicInfo.getPath());
                mediaPlayer.prepare();  //进行缓冲
            } catch (Exception e) {
                e.printStackTrace();
            }
            play(true);
            //启动子线程解析音乐图片并发送
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
                    fmmr.setDataSource(mMusicInfo.getPath());
                    byte[] cover_data = fmmr.getEmbeddedPicture();
                    if(cover_data != null) {
                        mBitmap = BitmapFactory.decodeByteArray(cover_data, 0, cover_data.length);
                    }
                    else {
                        mBitmap = null;
                    }
                    sendBroadcast();
                }
            }).start();

        }
    }

    //音乐播放完毕后回调
    private void setListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.seekTo(0);
                sendMessage();
                sendBroadcast();
            }
        });
    }

    //发送刷新UI界面的应用内广播
    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(UIREFRESH_PRIVATE);
        sendBroadcast(intent);
    }

}
