package cc.shinrai.eko;

import android.app.Application;
import android.content.Intent;

/**
 * Created by Shinrai on 2017/3/24 0024.
 */

public class RecApplication extends Application {
    private Intent sendIntent = null;
    private Boolean wifi_state = false;


    public Intent getSendIntent() {
        return sendIntent;
    }

    public void setSendIntent(Intent sendIntent) {
        this.sendIntent = sendIntent;
    }


    public Boolean getWifi_state() {
        return wifi_state;
    }

    public void setWifi_state(Boolean wifi_state) {
        this.wifi_state = wifi_state;
    }

}
