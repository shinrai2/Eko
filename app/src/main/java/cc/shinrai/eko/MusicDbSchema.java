package cc.shinrai.eko;

/**
 * Created by Shinrai on 2017/3/29 0029.
 */

public class MusicDbSchema {
    public static final class MusicTable {
        public static final String NAME = "musics";

        public static final class Cols {
            public static final String MUSIC_NAME = "music_name";
            public static final String SINGER_NAME = "singer_name";
            public static final String DURATION_TIME = "duration_time";
            public static final String PATH = "path";
        }
    }
}
