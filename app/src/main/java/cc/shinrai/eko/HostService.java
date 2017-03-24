package cc.shinrai.eko;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends IntentService {
    private static final String TAG = "HostService";
    private UdpServer udpServer = null;
    private static boolean flag = true; //关闭线程的标记

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HostService(String name) {
        super(name);
    }
    public HostService(){
        this(TAG);
        if(udpServer == null)
            udpServer = new UdpServer();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("Service", "onHandleIntent");
        while (flag) {
            udpServer.sendMessage(new SimpleDateFormat("hh:mm:ss").format(new java.util.Date()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static Intent newIntent(Context context) {
        flag = true;
        return new Intent(context, HostService.class);
    }

    public static void setTag(boolean b) {
        flag = b;
    }

    @Override
    public void onDestroy() {
        HostService.setTag(false);
        udpServer.closeSocket();
        super.onDestroy();
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        Log.i("HostService","onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("HostService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.i("HostService", "onCreate");
        super.onCreate();
    }
}
