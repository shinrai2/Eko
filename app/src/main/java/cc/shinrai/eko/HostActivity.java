package cc.shinrai.eko;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

public class HostActivity extends AppCompatActivity {

    private Button mWirelessButton;
    private WifiManager wifiManager;
    private boolean ap_state;//记录AP状态
    private boolean wifi_state = false;//记录开启AP前wifi状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWirelessButton = (Button)findViewById(R.id.wireless_button);
        if(isWifiApEnabled()) {
            mWirelessButton.setText(R.string.close_wireless_text);
            ap_state = true;
        }
        else {
            mWirelessButton.setText(R.string.open_wireless_text);
            ap_state = false;
        }

        mWirelessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是打开状态就关闭，如果是关闭就打开
                ap_state=!ap_state;
                boolean setApReturn = setWifiApEnabled(ap_state);
                if (!ap_state && wifi_state && setApReturn) {//判断是否要重新连接wifi
                    wifiManager.setWifiEnabled(true);
                    wifi_state = false;
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

        /**socket收到消息线程*/
        SocketServer.ServerHandler=new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                Toast.makeText(HostActivity.this,
                        msg.toString(),Toast.LENGTH_SHORT).show();
            }
        };
    }

    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            if(wifiManager.getWifiState() == 3) wifi_state = true;
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
}
