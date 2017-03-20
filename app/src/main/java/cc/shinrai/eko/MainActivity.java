package cc.shinrai.eko;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
    }
}
