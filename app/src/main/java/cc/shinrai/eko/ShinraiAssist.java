package cc.shinrai.eko;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Shinrai on 2017/5/9 0009.
 */

public class ShinraiAssist {
    /**
     * 格式化音乐时长的字符串成便于人阅读的时间字符串
     * @param durationTime 时间（毫秒）的字符串
     */
    public static String formatDurationTime(String durationTime) {
        int sec = Integer.parseInt(durationTime);
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String ms = formatter.format(sec);
        return ms;
    }
    /**
     * 格式化音乐时长的字符串成便于人阅读的时间字符串
     * @param sec 时间（毫秒）的数值
     */
    public static String formatDurationTime(int sec) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String ms = formatter.format(sec);
        return ms;
    }
}
