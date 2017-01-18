package demo.liqi.audioutils.utils.algorithm;

import demo.liqi.audioutils.utils.interfa.OnTimeFormatTransition;

/**
 * 把秒钟转换成特定时间格式对象
 * <p/>
 * 时间格式：00:00
 * Created by LiQi on 2017/1/18.
 */
public class SecondsTimeFormatText implements OnTimeFormatTransition {
    @Override
    public String onTimeFormatTransition(int duration) {
        int second = 0, minute = 0;
        if (duration >= 60) {
            //秒
            second = duration % 60;
            //分
            minute = (int) duration / 60;
        } else
            second = duration;

        return String.format("%1$02d:%2$02d", minute, second);
    }
}
