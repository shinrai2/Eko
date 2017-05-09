package cc.shinrai.eko;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wangjie.shadowviewhelper.ShadowProperty;
import com.wangjie.shadowviewhelper.ShadowViewHelper;

import org.w3c.dom.Text;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class HostActivity extends AppCompatActivity {
    public static final String  TAG                 = "HostActivity";
    public static final int     TIMER_REFRESH       = 7;
    public static final int     BACKGROUND_REFRESH  = 15;
    private static final int    radiusOfBlur        = 15;  //blur特效的半径
    private boolean             ap_state;           //记录AP状态
    private Button              mWirelessButton;
    private ImageButton         mPlay_image_button;
    private Button              mFileButton;
    private TextView            mMusicName;
    private TextView            mSingerName;
    private TextView            mDuringTimeTextView;
    private TextView            mLastTimeTextView;
    private ProgressBar         mProgressBar;
    private ImageView           mCoverView;
    private WifiManager         wifiManager;
    private HostService         hostService;
    private MusicInfo           mMusicInfo;
    private ContentReceiver     mReceiver;          //获取应用内广播的receiver
    private Timer               mTimer;             //音乐进度条的timer
    private TimerTask           mTimerTask;         //上述timer对应的timertask
    private Handler             mTimerHandler;      //更改进度条位置的handler
    private LinearLayout        mParentLinearlayout;
    private Handler             mUIHandler;

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
            UIandDataRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            hostService = null;
        }
    };

    //刷新UI界面和数据
    private void UIandDataRefresh() {
        mMusicInfo = hostService.getMusicInfo();
        if(mMusicInfo != null) {
            mMusicName.setText(mMusicInfo.getMusicName());
            mSingerName.setText(mMusicInfo.getSingerName());
            mProgressBar.setMax(Integer.parseInt(mMusicInfo.getDurationTime()));
            //设置音乐时长和剩余时长
            String during_time = ShinraiAssist.formatDurationTime(mMusicInfo.getDurationTime());
            mDuringTimeTextView.setText(during_time);
            mLastTimeTextView.setText("-" + during_time);
        }
        Bitmap bitmap = hostService.getBitmap();
        BitmapDrawable backgroundDrawable = hostService.getBlurBackgroundImage();
        if(bitmap != null) { //设置封面 ( null 则设置为默认封面)
            mCoverView.setImageBitmap(bitmap);
        }
        else {
            mCoverView.setImageResource(R.drawable.default_cover);
        }
        if(backgroundDrawable != null) { //设置背景 ( null 则设置为默认背景)
            mParentLinearlayout.setBackgroundDrawable(backgroundDrawable);
        }
        else {
            mParentLinearlayout.setBackgroundResource(R.drawable.alpha_45_dark);
            //重复调用，防止首次刷新失败导致往后再也无法生成背景
            BackgroundRefresh();
        }
        if(hostService.isPlaying() == false) {
            mPlay_image_button.setImageResource(R.drawable.xplay);
        }
        else {
            mPlay_image_button.setImageResource(R.drawable.xpause);

        }
    }

    /**
     * 当 hostService 中的背景对象被置空，重新运算计算出背景图，设置背景图并记录至 hostService
     */
    private void BackgroundRefresh() {
        if(hostService.getBlurBackgroundImage() == null) {
            final Bitmap bitmap = hostService.getBitmap();
            if(bitmap != null) {
                //开启线程处理耗时操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable backgroundDrawable;
                        //处理blur化的背景图
                        int widthOfParentLinearlayout = mParentLinearlayout.getWidth();
                        int heightOfParentLinearlayout = mParentLinearlayout.getHeight();

                        int heightOfBitmap = bitmap.getHeight();
                        int widthOfBitmap = bitmap.getWidth();

                        int scaledHeight = heightOfBitmap / 6;
                        int scaledWidth = widthOfBitmap / 6;

                        try {
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledHeight, scaledWidth, true);
                            int widthOfCut =
                                    (widthOfParentLinearlayout * scaledHeight) / heightOfParentLinearlayout;
                            int xTopLeft = (scaledWidth - widthOfCut) / 2;
                            int yTopLeft = 0;
                            Bitmap originBackgroundBitmap = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, widthOfCut, scaledHeight);
                            scaledBitmap.recycle();
                            Bitmap blurBitmap = FastBlurUtil.doBlur(originBackgroundBitmap, radiusOfBlur, true);
                            backgroundDrawable = new BitmapDrawable(blurBitmap);
                            //将对象记录到service中
                            hostService.setBlurBackgroundImage(backgroundDrawable);
                        } catch (Exception e) {
                            Log.i(TAG, e.toString());
                            backgroundDrawable = null;
                        }
                        Message backgroundRefreshMessage = new Message();
                        backgroundRefreshMessage.what = BACKGROUND_REFRESH;
                        backgroundRefreshMessage.obj = backgroundDrawable;
                        mUIHandler.sendMessage(backgroundRefreshMessage);
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        initState();
        setContentView(R.layout.activity_host);

        //初始化wifi管理、各种操作的按钮
        wifiManager         = (WifiManager)getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mWirelessButton     = (Button)      findViewById(R.id.wireless_button);
        mPlay_image_button  = (ImageButton) findViewById(R.id.play_image_button);
        mFileButton         = (Button)      findViewById(R.id.file_button);
        mMusicName          = (TextView)    findViewById(R.id.musicTitle);
        mSingerName         = (TextView)    findViewById(R.id.singerName);
        mCoverView          = (ImageView)   findViewById(R.id.musicCover);
        mProgressBar        = (ProgressBar) findViewById(R.id.progressBar);
        mParentLinearlayout = (LinearLayout)findViewById(R.id.parentLinearlayout);
        mDuringTimeTextView = (TextView)    findViewById(R.id.duringTimeTextView);
        mLastTimeTextView   = (TextView)    findViewById(R.id.lastTimeTextView);
        ShadowViewHelper.bindShadowHelper(new ShadowProperty()
                .setShadowColor(0x77000000)
                .setShadowDx(3)
                .setShadowDy(3)
                .setShadowRadius(5)
        , mCoverView);

        mUIHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BACKGROUND_REFRESH:
                        BitmapDrawable bd = (BitmapDrawable)msg.obj;
                        mParentLinearlayout.setBackgroundDrawable(bd);
                        break;
                    default:
                        break;
                }
            }
        };

        mTimer = new Timer();
        mTimerHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TIMER_REFRESH:
                        mProgressBar.setProgress(hostService.getCurrentPosition());
                        int lastTime = hostService.getLastTime();
                        //减少构造字符串，优化性能
                        if((lastTime % 1000) < 500) {
                            mLastTimeTextView.setText("-" +
                                    ShinraiAssist.formatDurationTime(lastTime));
                        }
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
                    hostService.play(false);
                    mPlay_image_button.setImageResource(R.drawable.xplay);
                }
                else {
                    hostService.play(true);
                    mPlay_image_button.setImageResource(R.drawable.xpause);
                }
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
        unregisterReceiver(mReceiver);
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
            if(hostService != null) {
                UIandDataRefresh();
                BackgroundRefresh();
            }
        }
    }

    /**
     * 沉浸式状态栏
     */
    private void initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
