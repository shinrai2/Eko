package cc.shinrai.eko;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

public class HostActivity extends AppCompatActivity {

    private Button mWirelessButton;
    private WifiManager wifiManager;
    private boolean flag = false;//记录AP状态
    private boolean wifi_state = false;//记录开启AP前wifi状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWirelessButton = (Button)findViewById(R.id.wireless_button);
        mWirelessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果是打开状态就关闭，如果是关闭就打开
                flag=!flag;
                setWifiApEnabled(flag);
                if (!flag && wifi_state) {//判断是否要重新连接wifi
                    wifiManager.setWifiEnabled(true);
                    wifi_state = false;
                }
                //Button标签切换
                if(flag) mWirelessButton.setText(R.string.close_wireless_text);
                else mWirelessButton.setText(R.string.open_wireless_text);
            }
        });
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
}
