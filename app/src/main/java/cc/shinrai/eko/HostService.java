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
    private static final String TAG = "HostService";
//    private UdpServer udpServer = null;
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

//    public void sendMessage() {
//        Log.i(TAG, "sendMessage");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "before send.");
//                while (flag) {
//                    udpServer.sendMessage(new SimpleDateFormat("hh:mm:ss")
//                            .format(new java.util.Date()));
//                    Log.i(TAG, "send.");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }

    public void stop() {

    }

    public void play() {

    }


    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    @Override
    public void onDestroy() {
        this.flag = false;
//        udpServer.closeSocket();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "start.");
//        if (udpServer == null)
//            udpServer = new UdpServer();
        super.onCreate();
    }
}
