package cc.shinrai.eko;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class UdpServer {
    private MulticastSocket mSocket;
    private DatagramPacket dataPacket = null;


    public UdpServer() {
        try {
            mSocket = new MulticastSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String msg) {
        sendData(msg.getBytes());
    }

    public void sendData(byte[] data) {
        Log.i("udp","before send.");

        try {
            mSocket.setTimeToLive(4);
            mSocket.setNetworkInterface(getWlanEth());
            //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
//            byte[] data = msg.getBytes();
            //224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName("224.0.0.1");
            //这个地方可以输出判断该地址是不是广播类型的地址
            System.out.println(address.isMulticastAddress());
            dataPacket = new DatagramPacket(data, data.length, address,
                    8004);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket.send(dataPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeSocket() {
        mSocket.close();
    }

    public static NetworkInterface getWlanEth() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        NetworkInterface wlan0 = null;
        StringBuilder sb = new StringBuilder();
        while (enumeration.hasMoreElements()) {
            wlan0 = enumeration.nextElement();
            sb.append(wlan0.getName() + " ");
            if (wlan0.getName().equals("wlan0")) {
                //there is probably a better way to find ethernet interface
                Log.i("wlan0", "wlan0 found");
                return wlan0;
            }
        }

        return null;
    }
}