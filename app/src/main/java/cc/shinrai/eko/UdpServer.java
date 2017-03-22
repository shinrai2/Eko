package cc.shinrai.eko;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Shinrai on 2017/3/22 0022.
 */

public class UdpServer {
    private MulticastSocket mSocket;
    private InetAddress group;


    public UdpServer() {
        try {
            mSocket = new MulticastSocket(7412);
            group = InetAddress.getByName("239.0.0.1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage() {
        try {
            byte[] buff = "QQ".getBytes("utf-8");
            mSocket.joinGroup(group);
            mSocket.setTimeToLive(4);
            DatagramPacket packet = new DatagramPacket(
                    buff, buff.length, group, 7412);
            mSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mSocket.close();
        }
    }
}
