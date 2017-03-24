package cc.shinrai.eko;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class HostActivity extends AppCompatActivity {
    private Button mWirelessButton;
    private Button mSendButton;
    private ImageView mMusicCover;
    private WifiManager wifiManager;
    private boolean ap_state;                   //记录AP状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_host);

        wifiManager = (WifiManager)getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mMusicCover = (ImageView)findViewById(R.id.musicCover);
        mWirelessButton = (Button)findViewById(R.id.wireless_button);
        mSendButton = (Button)findViewById(R.id.sned_button);
        if(isWifiApEnabled()) {
            mWirelessButton.setText(R.string.close_wireless_text);
            ap_state = true;
        }
        else {
            mWirelessButton.setText(R.string.open_wireless_text);
            ap_state = false;
        }

        if(getRec().getSendIntent() == null) {
            mSendButton.setText(R.string.send_it);
        }
        else {
            mSendButton.setText(R.string.stop_send);
        }

        //开启or关闭无线热点
        mWirelessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是打开状态就关闭，如果是关闭就打开

                ap_state=!ap_state;
                Boolean setApReturn = setWifiApEnabled(ap_state);       //设置AP是否成功
                Log.i("setWifiApEnabled : ", setApReturn.toString());

                boolean wifi_state = getRec().getWifi_state();

                if (!ap_state  && setApReturn && wifi_state) {           //判断是否要重新连接wifi
                    wifiManager.setWifiEnabled(true);
                    getRec().setWifi_state(false);
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

        //发送UDP包
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getRec().getSendIntent() == null) {
                    Intent tmpIntent = HostService.newIntent(HostActivity.this);
                    getRec().setSendIntent(tmpIntent);
                    startService(tmpIntent);
                    mSendButton.setText(R.string.stop_send);
                }
                else {
                    Boolean b = stopService(getRec().getSendIntent());
                    Log.i("onDestroy", "stopService : " + b.toString());
                    getRec().setSendIntent(null);
                    mSendButton.setText(R.string.send_it);
                }
            }
        });
    }

    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            if(wifiManager.getWifiState() == 3) {
                getRec().setWifi_state(true);
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
    protected void onDestroy() {
        super.onDestroy();
    }

    public RecApplication getRec() {
        return ((RecApplication)getApplicationContext());
    }
}
