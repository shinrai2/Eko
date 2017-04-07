package cc.shinrai.eko;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class MusicListActivity extends AppCompatActivity {
    public static final int RECYCLERVIEW_REFLESH = 1;
    public static final int MUSIC_LIST_UPDATE_REFLESH = 2;
    private static final String TAG = "MusicListActivity";
    private RecyclerView mMusicRecyclerView;
    private TextView mMusicTitleOnBar;//栏的音乐title
    private TextView mSingerNameOnBar;//栏的歌手名字
    private PercentRelativeLayout mPercentRelativeLayout;
    private MusicAdapter mAdapter;
    private List<MusicInfo> musicInfoList;
    private Handler mUIHandler;//启动时音乐读取时显示progressdialog的handler
    private MusicInfo mMusicInfo;
    private ImageView mPicView;
    private ProgressDialog mProgressDialog;
    private ContentReceiver mReceiver;

    private HostService hostService;
    //ServiceConnection
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hostService = ((HostService.MyBinder)service).getService();
            setBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            hostService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_list);

        mMusicRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        mPicView = (ImageView)findViewById(R.id.musicPic);
        //栏
        mMusicTitleOnBar = (TextView)findViewById(R.id.musicTitleOnBar);
        mSingerNameOnBar = (TextView)findViewById(R.id.singerNameBar);
        mPercentRelativeLayout = (PercentRelativeLayout)findViewById(R.id.bottomBar);
        mPercentRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MusicListActivity.this, HostActivity.class);
                startActivity(i);
            }
        });

        mMusicRecyclerView.setLayoutManager(
                new LinearLayoutManager(MusicListActivity.this));

        mProgressDialog = ProgressDialog.show(MusicListActivity.this, "Loading..", "Please wait..", true, false);

        //通过handler进行更新UI的操作
        mUIHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECYCLERVIEW_REFLESH:
                        //ProgressDialog消失，刷新列表
                        mProgressDialog.dismiss();
                        updateUI();
                        break;
                    case MUSIC_LIST_UPDATE_REFLESH:
                        //刷新ProgressDialog上面的文本显示
                        mProgressDialog.setMessage((String)msg.obj);
                    default:
                        break;
                }
            }
        };
        //在子线程中执行音乐列表读取操作并调用handler
        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicLab musicLab = MusicLab.get(MusicListActivity.this, mUIHandler);
                musicInfoList = musicLab.getMusicInfos();
                Message message = new Message();
                message.what = RECYCLERVIEW_REFLESH;
                mUIHandler.sendMessage(message);
            }
        }).start();

        mReceiver=new ContentReceiver();
        IntentFilter filter = new IntentFilter(
                HostService.UIREFRESH_PRIVATE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStart() {
        //启动后台服务
        Intent tmpIntent = HostService.newIntent(MusicListActivity.this);
        startService(tmpIntent);
        bindService(tmpIntent, sc, MusicListActivity.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(sc);
        super.onStop();
    }

    private void setBar() {
        //更新音乐信息
        mMusicInfo = hostService.getMusicInfo();
        //栏的textview内容更改和栏的可视操作
        Bitmap bitmap = hostService.getBitmap();
        if(bitmap != null) {
            mPicView.setImageBitmap(bitmap);
        }
        else {
            mPicView.setImageResource(R.drawable.default_cover);
        }
        if(mMusicInfo != null) {
            mPercentRelativeLayout.setVisibility(View.VISIBLE);
            mMusicTitleOnBar.setText(mMusicInfo.getMusicName());
            mSingerNameOnBar.setText(mMusicInfo.getSingerName());
        }
        else {
            mPercentRelativeLayout.setVisibility(View.GONE);
        }
    }

    //更新列表的UI操作
    protected void updateUI() {
        mAdapter = new MusicAdapter(musicInfoList);
        mMusicRecyclerView.setAdapter(mAdapter);
        mMusicRecyclerView.addItemDecoration(new RecycleViewDivider(MusicListActivity.this, LinearLayoutManager.HORIZONTAL));
    }



    private class MusicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mMusicNameTextView;
        private TextView mSingerNameTextView;
        private TextView mMusicTimeTextView;
        private MusicInfo musicInfo;

        public void bindMusic (MusicInfo m) {
            musicInfo = m;
            mMusicNameTextView.setText(musicInfo.getMusicName());
            mSingerNameTextView.setText(musicInfo.getSingerName());
            mMusicTimeTextView.setText(formatDurationTime(musicInfo.getDurationTime()));
        }

        //格式化音乐时长的字符串成便于人阅读的时间字符串
        private String formatDurationTime(String durationTime) {
            int sec = Integer.parseInt(durationTime);
            SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            String ms = formatter.format(sec);
            return ms;
        }

        public MusicHolder(View itemView) {
            super(itemView);

            mMusicNameTextView = (TextView)itemView.findViewById(R.id.music_name);
            mSingerNameTextView = (TextView)itemView.findViewById(R.id.singer_name);
            mMusicTimeTextView = (TextView)itemView.findViewById(R.id.music_time);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mMusicInfo = musicInfo;
            if(mMusicInfo != null) {
                hostService.prepare(mMusicInfo);
            }
//            setBar(musicInfo);
            Intent i = new Intent(MusicListActivity.this, HostActivity.class);
            startActivity(i);
        }
    }

    private class MusicAdapter extends RecyclerView.Adapter<MusicHolder> {
        private List<MusicInfo> mMusicInfoList;

        public MusicAdapter(List<MusicInfo> musicInfoList) {
            mMusicInfoList = musicInfoList;
        }

        @Override
        public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MusicListActivity.this);
            View view = layoutInflater
                    .inflate(R.layout.music_list_item, parent, false);
            return new MusicHolder(view);
        }

        @Override
        public void onBindViewHolder(MusicHolder holder, int position) {
            MusicInfo musicInfo = mMusicInfoList.get(position);
            holder.bindMusic(musicInfo);
        }

        @Override
        public int getItemCount() {
            return mMusicInfoList.size();
        }
    }

    private class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "ContentReceiver");
            setBar();
        }
    }

    @Override
    protected void onDestroy() {
        if (mReceiver!=null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }
}
