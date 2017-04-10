package cc.shinrai.eko;


import java.io.Serializable;

/**
 * Created by Shinrai on 2017/3/28 0028.
 */

public class MusicInfo implements Serializable {
    private String mMusicName;
    private String mSingerName;
    private String mDurationTime;
    private String mPath;

    //音乐名
    public String getMusicName() {
        return mMusicName;
    }
    public void setMusicName(String musicName) {
        mMusicName = musicName;
    }
    //歌手名
    public String getSingerName() {
        return mSingerName;
    }
    public void setSingerName(String singerName) {
        mSingerName = singerName;
    }
    //音乐时长
    public String getDurationTime() {
        return mDurationTime;
    }
    public void setDurationTime(String durationTime) {
        mDurationTime = durationTime;
    }
    //路径
    public String getPath() {
        return mPath;
    }
    public void setPath(String path) {
        mPath = path;
    }

    //判断两个实例是否相同(数值意义上)
    public static boolean equals_(MusicInfo a, MusicInfo b) {
        if(a != null && b != null) {
            if (a.getMusicName().equals(b.getMusicName()) &&
                    a.getSingerName().equals(b.getSingerName()) &&
                    a.getDurationTime().equals(b.getDurationTime())) {
                return true;
            }
        }
        return false;
    }
}
