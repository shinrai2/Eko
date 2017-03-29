package cc.shinrai.eko;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cc.shinrai.eko.MusicDbSchema.MusicTable;

/**
 * Created by Shinrai on 2017/3/29 0029.
 */

public class MusicBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "musicBase.db";

    public MusicBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + MusicTable.NAME + "(" +
            " _id integer primary key autoincrement, " +
            MusicTable.Cols.MUSIC_NAME + ", " +
            MusicTable.Cols.SINGER_NAME + ", " +
            MusicTable.Cols.DURATION_TIME +
            ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
