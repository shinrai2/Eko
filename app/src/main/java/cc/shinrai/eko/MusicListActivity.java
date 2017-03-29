package cc.shinrai.eko;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MusicListActivity extends AppCompatActivity {
    public static final int RECYCLERVIEW_REFLESH = 1;
    private RecyclerView mMusicRecyclerView;
    private MusicAdapter mAdapter;
    private List<MusicInfo> musicInfoList;
    private Handler mUIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_list);

        mMusicRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        mMusicRecyclerView.setLayoutManager(
                new LinearLayoutManager(MusicListActivity.this));

        mUIHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECYCLERVIEW_REFLESH:
                        updateUI();
                        break;
                    default:
                        break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicLab musicLab = MusicLab.get(MusicListActivity.this);
                musicInfoList = musicLab.getMusicInfos();
                Message message = new Message();
                message.what = RECYCLERVIEW_REFLESH;
                mUIHandler.sendMessage(message);
            }
        }).start();
    }


    protected void updateUI() {
        mAdapter = new MusicAdapter(musicInfoList);
        mMusicRecyclerView.setAdapter(mAdapter);
        mMusicRecyclerView.addItemDecoration(new RecycleViewDivider(MusicListActivity.this, LinearLayoutManager.HORIZONTAL));
    }



    private class MusicHolder extends RecyclerView.ViewHolder {
        private TextView mMusicNameTextView;
        private TextView mSingerNameTextView;
        private TextView mMusicTimeTextView;

        public MusicHolder(View itemView) {
            super(itemView);

            mMusicNameTextView = (TextView)itemView.findViewById(R.id.music_name);
            mSingerNameTextView = (TextView)itemView.findViewById(R.id.singer_name);
            mMusicTimeTextView = (TextView)itemView.findViewById(R.id.music_time);
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
            holder.mMusicNameTextView.setText(musicInfo.getMusicName());
            holder.mSingerNameTextView.setText(musicInfo.getSingerName());
            holder.mMusicTimeTextView.setText(musicInfo.getDurationTime());
        }

        @Override
        public int getItemCount() {
            return mMusicInfoList.size();
        }
    }
}
