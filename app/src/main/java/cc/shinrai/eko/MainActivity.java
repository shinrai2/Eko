package cc.shinrai.eko;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {

    private Button mHostButton;
    private Button mGuestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHostButton = (Button) findViewById(R.id.host);
        mGuestButton = (Button) findViewById(R.id.guset);
        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        R.string.host_tips,
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, HostActivity.class);
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
                                    //Toast.makeText(this, new String(buf, 0, dp.getLength()), Toast.LENGTH_LONG);
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
