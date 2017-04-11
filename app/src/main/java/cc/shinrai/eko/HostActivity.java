package cc.shinrai.eko;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wangjie.shadowviewhelper.ShadowProperty;
import com.wangjie.shadowviewhelper.ShadowViewHelper;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class HostActivity extends AppCompatActivity {
    public static final String  TAG = "HostActivity";
    public static final int     TIMER_REFRESH = 7;
    private boolean             ap_state;           //记录AP状态
    private Button              mWirelessButton;
    private ImageButton         mPlay_image_button;
    private Button              mFileButton;
    private TextView            mMusicName;
    private TextView            mSingerName;
    private ProgressBar         mProgressBar;
    private ImageView           mCoverView;
    private WifiManager         wifiManager;
    private HostService         hostService;
    private MusicInfo           mMusicInfo;
    private ContentReceiver     mReceiver;          //获取应用内广播的receiver
    private Timer               mTimer;             //音乐进度条的timer
    private TimerTask           mTimerTask;         //上述timer对应的timertask
    private Handler             mTimerHandler;      //更改进度条位置的handler

    //Service绑定后回调
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hostService = ((HostService.MyBinder)service).getService();
            if(mTimerTask == null) {
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = TIMER_REFRESH;
                        mTimerHandler.sendMessage(msg);
                    }
                };
                mTimer.schedule(mTimerTask, 0, 50);
            }
            //刷新UI
            UIrefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            hostService = null;
        }
    };

    //刷新UI界面
    private void UIrefresh() {
        mMusicInfo = hostService.getMusicInfo();
        if(mMusicInfo != null) {
            mMusicName.setText(mMusicInfo.getMusicName());
            mSingerName.setText(mMusicInfo.getSingerName());
            mProgressBar.setMax(Integer.parseInt(mMusicInfo.getDurationTime()));
        }
        if(hostService.isPlaying() == false) {
            mPlay_image_button.setImageResource(R.drawable.play);
        }
        else {
            mPlay_image_button.setImageResource(R.drawable.pause);

        }
        Bitmap bitmap = hostService.getBitmap();
        if(bitmap != null) {
            mCoverView.setImageBitmap(bitmap);
        }
        else {
            mCoverView.setImageResource(R.drawable.default_cover);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_host);

        //初始化wifi管理、各种操作的按钮
        wifiManager      = (WifiManager)getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mWirelessButton  = (Button)findViewById(R.id.wireless_button);
        mPlay_image_button = (ImageButton)findViewById(R.id.play_image_button);
        mFileButton      = (Button)findViewById(R.id.file_button);
        mMusicName       = (TextView)findViewById(R.id.musicTitle);
        mSingerName      = (TextView)findViewById(R.id.singerName);
        mCoverView       = (ImageView)findViewById(R.id.musicCover);
        mProgressBar     = (ProgressBar)findViewById(R.id.progressBar);
        ShadowViewHelper.bindShadowHelper(new ShadowProperty()
                .setShadowColor(0x77000000)
                .setShadowDx(3)
                .setShadowDy(3)
                .setShadowRadius(5)
        , mCoverView);

        mTimer = new Timer();
        mTimerHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TIMER_REFRESH:
                        mProgressBar.setProgress(hostService.getCurrentPosition());
                        break;
                    default:
                        break;
                }
            }
        };

        //判断ap是否已经开启
        if(isWifiApEnabled()) {
            mWirelessButton.setText(R.string.close_wireless_text);
            ap_state = true;
        }
        else {
            mWirelessButton.setText(R.string.open_wireless_text);
            ap_state = false;
        }

        //开启or关闭无线热点
        mWirelessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是打开状态就关闭，如果是关闭就打开

                ap_state=!ap_state;
                Boolean setApReturn = setWifiApEnabled(ap_state);       //设置AP是否成功
                Log.i("setWifiApEnabled : ", setApReturn.toString());

                boolean wifi_state = hostService.getWifi_state();

                if (!ap_state  && setApReturn && wifi_state) {           //判断是否要重新连接wifi
                    wifiManager.setWifiEnabled(true);
                    hostService.setWifi_state(false);
                }

                //Button标签切换
                if(ap_state) {
                    mWirelessButton.setText(R.string.close_wireless_text);
                }
                else {
                    mWirelessButton.setText(R.string.open_wireless_text);
                }
            }
        });

        //播放/暂停音乐
        mPlay_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hostService.isPlaying()) {
//                    hostService.setFlag(false);//按钮及启用代码状态标记
                    hostService.play(false);
                }
                else {
//                    hostService.setFlag(true);
                    hostService.play(true);
                }
                UIrefresh();
            }
        });

        //File预留按钮、待编辑
        mFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mReceiver=new ContentReceiver();
        IntentFilter filter = new IntentFilter(
                HostService.UIREFRESH_PRIVATE);
        registerReceiver(mReceiver, filter);
    }

    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            if(wifiManager.getWifiState() == 3) {
                hostService.setWifi_state(true);
            }
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "Eko";
            //配置热点的密码
            apConfig.preSharedKey="eko000000";
            apConfig.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.status = WifiConfiguration.Status.ENABLED;
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }
    //检测AP状态
    public boolean isWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }
    private WIFI_AP_STATE getWifiApState(){
        int tmp;
        try {
            Method method = wifiManager.getClass().getMethod(
                    "getWifiApState");
            tmp = ((Integer) method.invoke(wifiManager));
            // Fix for Android 4
            if (tmp > 10) {
                tmp = tmp - 10;
            }
            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }
    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING,  WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
    }

    @Override
    protected void onStart() {
        //启动后台服务
        Intent tmpIntent = HostService.newIntent(HostActivity.this);
        startService(tmpIntent);
        bindService(tmpIntent, sc, HostActivity.BIND_AUTO_CREATE);

        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(sc);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //注销广播接收器
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        //关闭进度条timer
        if(mTimerTask != null) {
            mTimerTask.cancel();
        }
        if(mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroy();
    }

    private class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "ContentReceiver");
            UIrefresh();
        }
    }
}
