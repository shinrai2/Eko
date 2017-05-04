package cc.shinrai.eko;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Shinrai on 2017/5/4 0004.
 */

public class UdpClient {
    private static final String TAG = "UdpClient";
    private static final String _multicastHost = "224.0.0.1";
    private static final String _regex = "";
    private MulticastSocket     multicastSocket;
    private InetAddress         receiveAddress;
    private boolean             isStopReceive = false;
    private List<String>        addressList;

    public UdpClient() {
        addressList = new ArrayList<>();
    }

    /**
     * receive UDP boardcast.
     */
    public void receive() {
        isStopReceive = false;
        try {
            multicastSocket = new MulticastSocket(8004);
            receiveAddress=InetAddress.getByName(_multicastHost);
            multicastSocket.joinGroup(receiveAddress);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte buf[] = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, 1024);
                    while (isStopReceive) {
                        try {
                            multicastSocket.receive(dp);
                            String info = new String(buf, 0, dp.getLength());
                            //解析并添加到列表中
                            addAddress(parseRegexString(info, _regex));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    multicastSocket.close();
                }
            }).start();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * parse the regexString.
     * @param string
     * @param regex
     */
    private String parseRegexString(String string, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string);
        if (m.find()) {
        }
        return null;
    }

    private void addAddress(String address) {
        if(!addressList.contains(address)) {
            addressList.add(address);
        }
    }

    private void deleteAddress(String address) {
        if(addressList.contains(address)) {
            addressList.remove(address);
        }
    }

    public void stopReceive() {
        isStopReceive = true;
    }
}
