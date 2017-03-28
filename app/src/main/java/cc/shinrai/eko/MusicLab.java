package cc.shinrai.eko;

import android.content.Context;

import java.util.List;

/**
 * Created by Shinrai on 2017/3/29 0029.
 */

public class MusicLab {
    private static MusicLab sMusicLab;
    private List<MusicInfo> mMusicInfoList;

    private MusicLab(Context context) {
        //从数据库中读取数据
        query();
    }

    public static MusicLab get(Context context) {
        if(sMusicLab == null) {
            sMusicLab = new MusicLab(context);
        }
        return sMusicLab;
    }

    public List<MusicInfo> getMusicInfo() {
        return mMusicInfoList;
    }

    //刷新音乐数据并记录到数据库中
    public void update() {
    }

    //从数据库中查询音乐数据
    public void query() {
    }
}
