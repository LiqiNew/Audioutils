package demo.liqi.audioutils.utils.algorithm;

import demo.liqi.audioutils.utils.interfa.OnTimeFormatTransition;

/**
 * 把秒钟转换成特定时间格式对象
 * 时间格式：1'3"
 * Created by LiQi on 2017/1/18.
 */
public class SecondsTimeFormatTime implements OnTimeFormatTransition {
    @Override
    public String onTimeFormatTransition(int duration) {
        String date = "0s";
        int second = 0, minute = 0;
        if (duration >= 60) {
            //秒
            second = duration % 60;
            //分
            minute = (int) duration / 60;
        } else
            second = duration;
        if (second > 0 && minute > 0) {
            date = minute + "'" + second + "\"";
        } else if (minute <= 0 && second > 0) {
            date = second + "s";
        } else if (minute > 0 && second <= 0) {
            date = minute + "'";
        }
        return date;
    }
}
