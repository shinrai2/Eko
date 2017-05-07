package cc.shinrai.eko;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {

    private ImageButton mHostButton;
    private ImageButton mGuestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHostButton = (ImageButton) findViewById(R.id.hostButton);
        mGuestButton = (ImageButton) findViewById(R.id.gusetButton);
        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //启动音乐列表Activity
                Intent i = new Intent(MainActivity.this, MusicListActivity.class);
                startActivity(i);
            }
        });
        mGuestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final MulticastSocket ds = new MulticastSocket(8004);
                    InetAddress receiveAddress=InetAddress.getByName("224.0.0.1");
                    ds.joinGroup(receiveAddress);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            byte buf[] = new byte[1024];
                            DatagramPacket dp = new DatagramPacket(buf, 1024);
                            while (true) {
                                try {
                                    ds.receive(dp);
                                    Log.i("Receive Time", new String(buf, 0, dp.getLength()));
//			                break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
    }
}
