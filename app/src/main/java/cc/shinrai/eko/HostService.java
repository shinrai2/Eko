package cc.shinrai.eko;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends Service {
    private UdpServer udpServer = null;
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

    public void sendMessage() {
        Log.i("HostService", "sendMessage");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("HostService", "before send.");
                while (flag) {
                    udpServer.sendMessage(new SimpleDateFormat("hh:mm:ss")
                            .format(new java.util.Date()));
                    Log.i("HostService", "send.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    @Override
    public void onDestroy() {
        udpServer.closeSocket();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i("HostService", "start.");
        if (udpServer == null)
            udpServer = new UdpServer();
        super.onCreate();
    }
}
