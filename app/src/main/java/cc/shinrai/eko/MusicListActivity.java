package cc.shinrai.eko;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wangjie.shadowviewhelper.ShadowProperty;
import com.wangjie.shadowviewhelper.ShadowViewHelper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;


public class MusicListActivity extends AppCompatActivity {
    public static final int     RECYCLERVIEW_REFLESH        = 1;
    public static final int     MUSIC_LIST_UPDATE_REFLESH   = 2;
    private static final String TAG                         = "MusicListActivity";
    private RecyclerView        mMusicRecyclerView;
    private TextView            mMusicTitleOnBar;               //栏的音乐title
    private TextView            mSingerNameOnBar;               //栏的歌手名字
    private PercentRelativeLayout mPercentRelativeLayout;
    private MusicAdapter        mAdapter;
    private Handler             mUIHandler;                     //启动时音乐读取时显示progressdialog的handler
    private MusicInfo           mMusicInfo;
    private ImageView           mPicView;
    private ProgressDialog      mProgressDialog;
    private ContentReceiver     mReceiver;

    private HostService hostService;
    //ServiceConnection
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            hostService = ((HostService.MyBinder)service).getService();
            //判断hostService是否已存在音乐列表，不存在即开启线程异步获取列表
            if(hostService.getMusicInfoList() == null) {
                //在子线程中执行音乐列表读取操作并调用handler
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //耗时操作，读取(首次则扫描)并封装音乐的相关数据
                        MusicLab musicLab = MusicLab.get(MusicListActivity.this, mUIHandler);
                        hostService.setMusicInfoList(musicLab.getMusicInfos());
                        //更新recyclerview
                        Message message = new Message();
                        message.what = RECYCLERVIEW_REFLESH;
                        mUIHandler.sendMessage(message);
                    }
                }).start();
                //刷新数据和ui
                UIandDataRefresh();
            }
            else {
                Message message = new Message();
                message.what = RECYCLERVIEW_REFLESH;
                mUIHandler.sendMessage(message);
                //刷新数据和ui
                UIandDataRefresh();
            }
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
        initState();
        setContentView(R.layout.activity_music_list);

        mMusicRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        //栏
        mPicView = (ImageView)findViewById(R.id.musicPic);
        ShadowViewHelper.bindShadowHelper(new ShadowProperty()
                        .setShadowColor(0x77000000)
                        .setShadowDx(3)
                        .setShadowDy(3)
                        .setShadowRadius(5)
                , mPicView);
        mMusicTitleOnBar         = (TextView)findViewById(R.id.musicTitleOnBar);
        mSingerNameOnBar         = (TextView)findViewById(R.id.singerNameBar);
        mPercentRelativeLayout   = (PercentRelativeLayout)findViewById(R.id.bottomBar);
        mPercentRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MusicListActivity.this, HostActivity.class);
                startActivity(i);
            }
        });
        mPercentRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMusicRecyclerView.scrollToPosition(hostService.getCurrentMusicPosition());
                return true;
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
                        updateList();
                        break;
                    case MUSIC_LIST_UPDATE_REFLESH:
                        //刷新ProgressDialog上面的文本显示
                        mProgressDialog.setMessage((String)msg.obj);
                    default:
                        break;
                }
            }
        };

        mReceiver=new ContentReceiver();
        IntentFilter filter = new IntentFilter(
                HostService.UIREFRESH_PRIVATE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStart() {
        //启动后台服务
        Intent intent = HostService.newIntent(MusicListActivity.this);
        startService(intent);
        bindService(intent, sc, MusicListActivity.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(sc);
        super.onStop();
    }

    //刷新UI界面和数据
    private void UIandDataRefresh() {
        //更新音乐信息
        mMusicInfo       = hostService.getMusicInfo();
        //栏的textview内容更改和栏的可视操作
        Bitmap bitmap    = hostService.getBitmap();
        if(bitmap != null) { //设置封面 ( null 则设置为默认封面)
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
    protected void updateList() {
        if (mAdapter == null) {
            mAdapter = new MusicAdapter(hostService.getMusicInfoList());
            mMusicRecyclerView.setAdapter(mAdapter);
            mMusicRecyclerView.addItemDecoration(new RecycleViewDivider(MusicListActivity.this, LinearLayoutManager.HORIZONTAL));
        }
    }

    private class MusicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView        mMusicNameTextView;
        private TextView        mSingerNameTextView;
        private TextView        mMusicTimeTextView;
        private MusicInfo       musicInfo;
        private ImageView       isPlaying;
        private TextView        numberOfMusic;

        public void bindMusic (MusicInfo m, int position) {
            musicInfo = m;
            mMusicNameTextView.setText(musicInfo.getMusicName());
            mSingerNameTextView.setText(musicInfo.getSingerName());
            mMusicTimeTextView.setText(formatDurationTime(musicInfo.getDurationTime()));
            numberOfMusic.setText(((Integer)position).toString());
            //播放ui调整
            musicItemRefresh();
        }

        //调整播放中item的效果显示
        private void musicItemRefresh() {
            if(musicInfo.isCurrentMusic()) {
                isPlaying.setVisibility(View.VISIBLE);
                numberOfMusic.setVisibility(View.INVISIBLE);
                mMusicNameTextView.setTextColor(getResources().getColor(R.color.colorText_red));
            }
            else {
                isPlaying.setVisibility(View.INVISIBLE);
                numberOfMusic.setVisibility(View.VISIBLE);
                mMusicNameTextView.setTextColor(getResources().getColor(R.color.colorText));
            }
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
            mMusicNameTextView   = (TextView)itemView.findViewById(R.id.music_name);
            mSingerNameTextView  = (TextView)itemView.findViewById(R.id.singer_name);
            mMusicTimeTextView   = (TextView)itemView.findViewById(R.id.music_time);
            isPlaying            = (ImageView)itemView.findViewById(R.id.isPlaying);
            numberOfMusic        = (TextView)itemView.findViewById(R.id.number_of_music);
            itemView.setOnClickListener(this);
        }

        //点击列表项目
        @Override
        public void onClick(View v) {
            mMusicInfo = musicInfo;
            if(mMusicInfo != null) {
                hostService.prepare(mMusicInfo);
            }
            //通知recyclerview更新
            mAdapter.notifyDataSetChanged();
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
            holder.bindMusic(musicInfo, position);
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
            //刷新数据和ui
            UIandDataRefresh();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /**
     * 沉浸式状态栏
     */
    private void initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
