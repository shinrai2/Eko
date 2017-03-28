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

public class MusicListActivity extends AppCompatActivity {
    private RecyclerView mMusicRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_music_list);

        mMusicRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        mMusicRecyclerView.setLayoutManager(
                new LinearLayoutManager(MusicListActivity.this));
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

        @Override
        public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(MusicListActivity.this);
            View view = layoutInflater
                    .inflate(R.layout.music_list_item, parent, false);
            return new MusicHolder(view);
        }

        @Override
        public void onBindViewHolder(MusicHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
