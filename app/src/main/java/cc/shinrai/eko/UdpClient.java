package cc.shinrai.eko;

import android.os.Handler;
import android.os.Message;
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
    private static final String TAG             = "UdpClient";
    private static final String _multicastHost  = "224.0.0.1";
    private static final String _regex          = "k((?:[0-9]{1,3}\\.){3}[0-9]{1,3})";//format like 'k192.168.199.1'
    private MulticastSocket     multicastSocket;
    private InetAddress         receiveAddress;
    private boolean             isStopReceive   = false;
    private List<String>        addressList;
    private Handler             newConnectHandler;

    public UdpClient(Handler handler) {
        //Init list.
        this.addressList = new ArrayList<>();
        this.newConnectHandler = handler;
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
                    while (!isStopReceive) {
                        try {
                            multicastSocket.receive(dp);
                            String info = new String(buf, 0, dp.getLength());
                            Log.i(TAG, info);
                            //通过handler向解析的ip发送请求
                            Message msg = new Message();
                            msg.what = HostService.NEW_CONNECT_USER;
                            msg.obj = parseRegexString(info, _regex);
                            newConnectHandler.sendMessage(msg);
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
            return m.group(1).toString();
        }
        return null;
    }

    public void addAddress(String address) {
        //如果地址在列表中不存在
        if(!addressList.contains(address)) {
            addressList.add(address);
        }
    }
    //tcp发送失败时，删除记录
    public void deleteAddress(String address) {
        if(addressList.contains(address)) {
            addressList.remove(address);
        }
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public void stopReceive() {
        isStopReceive = true;
    }
}
