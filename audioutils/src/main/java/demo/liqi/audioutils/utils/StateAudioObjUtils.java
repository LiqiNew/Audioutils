package demo.liqi.audioutils.utils;

import android.content.Context;

/**
 * 音频录制工厂对象
 * Created by LiQi on 2016/12/28.
 */
public class StateAudioObjUtils {
    /**
     * 获取音频录制对象 WAV
     *
     * @param context
     * @return
     */
    public static MediaRecorderUtils getRecorderUtils(Context context) {
        return new MediaRecorderUtils(context);
    }

    /**
     * 获取音频录制对象 AMR
     *
     * @param context
     * @return
     */
    public static MediaRecorderUtilsAmr getRecorderUtilsAmr(Context context) {
        return new MediaRecorderUtilsAmr(context);
    }
}
