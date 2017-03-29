package cc.shinrai.eko;

/**
 * Created by Shinrai on 2017/3/28 0028.
 */

public class MusicInfo {
    private String mMusicName;
    private String mSingerName;
    private String mDurationTime;
    private String mPath;

    public String getMusicName() {
        return mMusicName;
    }
    public void setMusicName(String musicName) {
        mMusicName = musicName;
    }

    public String getSingerName() {
        return mSingerName;
    }
    public void setSingerName(String singerName) {
        mSingerName = singerName;
    }

    public String getDurationTime() {
        return mDurationTime;
    }
    public void setDurationTime(String durationTime) {
        mDurationTime = durationTime;
    }

    public String getPath() {
        return mPath;
    }
    public void setPath(String path) {
        mPath = path;
    }
}
