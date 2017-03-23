package cc.shinrai.eko;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class HostService extends IntentService {
    private static final String TAG = "HostService";
    private UdpServer udpServer = null;

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

    }

    public static Intent newIntent(Context context) {
        return new Intent(context, HostService.class);
    }
}
