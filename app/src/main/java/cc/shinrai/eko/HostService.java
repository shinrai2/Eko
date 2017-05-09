package cc.shinrai.eko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.Date;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends Service {
    public static final int     NEW_CONNECT_USER    = 11;
    public static final int     DISCONNECT_USER     = 13;
    public static final int     CYCLE_SWITCH        = 21;
    public static final int     REPEAT_ONE_SWITCH   = 23;
    public static final int     SHUFFLE_SWITCH      = 27;
    public static final String  UIREFRESH_PRIVATE   = "cc.shinrai.eko.UIREFRESH_PRIVATE";
    private static final String TAG = "HostService";
    private UdpClient           mUdpClient;
    private TcpServer           mTcpServer;
    private Handler             ConnectHandler;

    private MediaPlayer         mediaPlayer         = new MediaPlayer();
    private MusicInfo           mMusicInfo;
    private Bitmap              mBitmap;
    private Boolean             wifi_state          = false;
    private List<MusicInfo>     musicInfoList;
    private int                 musicSwitchMode     = CYCLE_SWITCH; //音乐切换模式标记，默认列表循环
    private BitmapDrawable      blurBackgroundImage = null;

    //记录经过处理的背景图片对象
    public void setBlurBackgroundImage(BitmapDrawable bitmapDrawable) {
        blurBackgroundImage = bitmapDrawable;
    }
    //获取背景图片对象
    public BitmapDrawable getBlurBackgroundImage() {
        return blurBackgroundImage;
    }
    //获取当前歌曲信息的排列位置
    public int getCurrentMusicPosition() {
        if (mMusicInfo == null) {
            return -1;
        }
        return musicInfoList.indexOf(mMusicInfo);
    }
    //获取指定音乐信息的目录位置
    public int getMusicPosition(MusicInfo musicInfo) {
        return musicInfoList.indexOf(musicInfo);
    }
    //获取音乐切换模式
    public int getMusicSwitchMode() {
        return musicSwitchMode;
    }
    //设定音乐切换模式
    public void setMusicSwitchMode(int musicSwitchMode) {
        this.musicSwitchMode = musicSwitchMode;
    }
    //获取音乐列表
    public List<MusicInfo> getMusicInfoList() {
        return musicInfoList;
    }
    //设定音乐列表
    public void setMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList = musicInfoList;
    }

    public Boolean getWifi_state() {
        return wifi_state;
    }

    public void setWifi_state(Boolean wifi_state) {
        this.wifi_state = wifi_state;
    }

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

//    public void sendMessage() {
//        Log.i(TAG, "sendMessage");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "before send.");
//                //构造包含同步消息的字符串
//                String msg = mMusicInfo.getMusicName() + "-" + mMusicInfo.getDurationTime() +
//                        "-" + mediaPlayer.isPlaying() + "-" + mediaPlayer.getCurrentPosition() +
//                        "-" + new Date().getTime();
//                udpServer.sendMessage(msg);
//                Log.i(TAG, msg + " had sent.");
//            }
//        }).start();
//    }

    /**
     * send data and file by tcp.
     * @param path path of file.null means only data.
     * @param specifyAddress send the data and file to a specify address.null means send to all addresses.
     */
    private void tcpSend(String path, String specifyAddress) {
        Log.i(TAG, "path : " + path + " specifyAddress : " + specifyAddress);
        String msg = getCurrentMusicPosition() + "-" + getCurrentPosition() +
                "-" + new Date().getTime() + "-" + (path!=null?"1":"0");
        List<String> addressList = mUdpClient.getAddressList();
        if(specifyAddress == null) {
            for(String address : addressList) {
                mTcpServer.connect(address, msg, path);
            }
        }
        else {
            mTcpServer.connect(specifyAddress, msg, path);
        }
    }
    //改变播放状态(操作用)
    public void play(boolean State) {
        if(State) {
            mediaPlayer.start();
        }
        else {
            mediaPlayer.pause();
        }
//        sendMessage();
        tcpSend(null, null);
    }

    //获取播放状态 TRUE = 播放 FALSE = 暂停
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    //获取目前播放的位置
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    };
    public int getLastTime() {
        return mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    @Override
    public void onDestroy() {
//        udpServer.closeSocket();
//        mSocketServer.stopSocket();
        mUdpClient.stopReceive();
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "start.");
        setMusicCompletionListener();
//        mHandler = new Handler() {
//
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case SEND_MESSAGE:
//                        HostService.this.sendMessage();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        };
//        onSocketStart();
        ConnectHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NEW_CONNECT_USER:
                        tcpSend(MusicLab.getBasePath() + mMusicInfo.getPath(), (String) msg.obj);
                        break;
                    case DISCONNECT_USER:
                        mUdpClient.deleteAddress((String) msg.obj);
                    default:
                        break;
                }
            }
        };
        mUdpClient = new UdpClient(ConnectHandler);
        mTcpServer = new TcpServer(ConnectHandler);
        mUdpClient.receive();
        super.onCreate();
    }

//    public void onSocketStart() {
//        //udp & tcp 启动
//        udpServer = new UdpServer();
//        mSocketServer = new SocketServer(9999, mHandler);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mSocketServer.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }

    //缓冲音乐并开始播放
    public void prepare(MusicInfo musicInfo) {
        if(MusicInfo.equals_(musicInfo, mMusicInfo) == false) {
            //清除上一张图片的缓存
            mBitmap = null;
            blurBackgroundImage = null;
            //切换现行播放歌曲的标记
            if(mMusicInfo != null) {
                mMusicInfo.setCurrentMusic(false);
            }
            musicInfo.setCurrentMusic(true);

            final String tmpath = MusicLab.getBasePath() + musicInfo.getPath();
            //发送tcp
            tcpSend(tmpath, null);

            mMusicInfo = musicInfo;
            //设置发送文件的路径
//            mSocketServer.setPath(tmpath);
            if(mediaPlayer.isPlaying() == true) {
                mediaPlayer.pause();
            }
            try {
                mediaPlayer.reset();//把各项参数恢复到初始状态
                mediaPlayer.setDataSource(tmpath);
                mediaPlayer.prepare();  //进行缓冲
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            //启动子线程解析音乐封面并发送通知广播
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
                    fmmr.setDataSource(tmpath);
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
        else {
            if(mediaPlayer.isPlaying() == false) {
                mediaPlayer.start();
            }
        }
    }

    //音乐播放完毕后回调
    private void setMusicCompletionListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMusicInfo.setCurrentMusic(false);
                mediaPlayer.seekTo(0);
                //通知播放下一首歌
                callNextMusic();
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

    private void callNextMusic() {

        switch (musicSwitchMode) {
            case CYCLE_SWITCH://列表循环
                int index = getCurrentMusicPosition();
                if(musicInfoList.size() > index + 1) {
                    prepare(musicInfoList.get(index + 1));
                }
                else if(!musicInfoList.isEmpty()) {
                    prepare(musicInfoList.get(0));
                }
                break;
            case REPEAT_ONE_SWITCH://单一循环
                mediaPlayer.start();
                break;
            case SHUFFLE_SWITCH://随机循环
                break;
            default:
                break;
        }
    }
}
