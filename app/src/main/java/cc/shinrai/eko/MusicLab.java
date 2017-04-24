package cc.shinrai.eko;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.shinrai.eko.MusicDbSchema.MusicTable;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Shinrai on 2017/3/29 0029.
 */

public class MusicLab {
    private static final String TAG = "MusicLab";
    private static MusicLab     sMusicLab;
    private List<MusicInfo>     mMusicInfoList;
    private Context             mContext;
    private SQLiteDatabase      mDatabase;
    private static final String basePath = "/storage/sdcard1/netease/cloudmusic/Music/";
    private Handler             mHandler;

    public static String getBasePath() {
        return basePath;
    }

    private MusicLab(Context context, Handler handler) {
        //从数据库中读取数据
        mContext         = context;
        mHandler         = handler;
        mDatabase        = new MusicBaseHelper(mContext)
                .getWritableDatabase();
        mMusicInfoList   = new ArrayList<>();
        Log.i(TAG, "query");
        query();
    }

    public static MusicLab get(Context context, Handler handler) {
        if(sMusicLab == null) {
            sMusicLab = new MusicLab(context, handler);
        }
        return sMusicLab;
    }

    public List<MusicInfo> getMusicInfos() {
        return mMusicInfoList;
    }

    //刷新音乐数据并记录到数据库中
    public void update() {
        //先进行数据库清空操作
        clean();

        File[] files = new File(basePath).listFiles();
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        for(File file : files) {
            Log.i(TAG, file.getPath());
            if(file.getName().matches(".*\\.mp3$")) {
                String path = file.getName();
                fmmr.setDataSource(basePath + path);
                String music_name    = fmmr.extractMetadata(
                        FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
                String singer_name   = fmmr.extractMetadata(
                        FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
                String duration_time = fmmr.extractMetadata(
                        FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

                if(music_name == null)  music_name  = "unknown";
                if(singer_name == null) singer_name = "unknown";
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.setMusicName(music_name);
                musicInfo.setSingerName(singer_name);
                musicInfo.setDurationTime(duration_time);
                musicInfo.setPath(path);
                addMusicInfo(musicInfo);
                mMusicInfoList.add(musicInfo);
                Message message = new Message();
                message.what = MusicListActivity.MUSIC_LIST_UPDATE_REFLESH;
                //刷新progressdialog信息，避免糟糕的用户体验(!)
                message.obj = "Add music : \n" + music_name + " - " + singer_name + " ..";
                mHandler.sendMessage(message);
            }
        }
    }

    //从数据库中查询音乐数据
    public void query() {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + MusicTable.NAME, null);
        while (cursor.moveToNext()) {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setMusicName(cursor.getString(cursor.getColumnIndex(MusicTable.Cols.MUSIC_NAME)));
            musicInfo.setSingerName(cursor.getString(cursor.getColumnIndex(MusicTable.Cols.SINGER_NAME)));
            musicInfo.setDurationTime(cursor.getString(cursor.getColumnIndex(MusicTable.Cols.DURATION_TIME)));
            musicInfo.setPath(cursor.getString(cursor.getColumnIndex(MusicTable.Cols.PATH)));

            mMusicInfoList.add(musicInfo);
        }
        if (mMusicInfoList.isEmpty()) {
            Log.i(TAG, "update");
            update();
        }
    }

    private static ContentValues getContentValues(MusicInfo musicInfo) {
        ContentValues values = new ContentValues();
        values.put(MusicTable.Cols.MUSIC_NAME, musicInfo.getMusicName());
        values.put(MusicTable.Cols.SINGER_NAME, musicInfo.getSingerName());
        values.put(MusicTable.Cols.DURATION_TIME, musicInfo.getDurationTime());
        values.put(MusicTable.Cols.PATH, musicInfo.getPath());

        return values;
    }

    //加入项目到数据库
    public void addMusicInfo(MusicInfo musicInfo) {
        ContentValues values = getContentValues(musicInfo);

        mDatabase.insert(MusicTable.NAME, null, values);
    }

    //清空数据库
    public void clean() {
        mDatabase.execSQL("DELETE FROM " + MusicTable.NAME);
        mMusicInfoList.clear();
    }
}
