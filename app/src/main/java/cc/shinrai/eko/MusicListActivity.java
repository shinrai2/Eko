package cc.shinrai.eko;

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
    private RecyclerView mMusicRecyclerView;
    private MusicAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_list);

        mMusicRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        mMusicRecyclerView.setLayoutManager(
                new LinearLayoutManager(MusicListActivity.this));
    }

    private void updateUI() {
        MusicLab musicLab = MusicLab.get(MusicListActivity.this);
        List<MusicInfo> musicInfoList = musicLab.getMusicInfo();
        mAdapter = new MusicAdapter(musicInfoList);
        mMusicRecyclerView.setAdapter(mAdapter);
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
            holder.mMusicTimeTextView.setText(musicInfo.getMusicTime());
        }

        @Override
        public int getItemCount() {
            return mMusicInfoList.size();
        }
    }
}
