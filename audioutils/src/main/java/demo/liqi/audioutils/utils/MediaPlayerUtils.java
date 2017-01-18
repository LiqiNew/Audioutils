package demo.liqi.audioutils.utils;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import demo.liqi.audioutils.utils.interfa.OnTimeFormatTransition;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 播放辅助对象工具
 * Created by LiQi on 2016/12/20.
 */
public class MediaPlayerUtils implements MediaPlayer.OnCompletionListener {
    private static MediaPlayerUtils sMediaPlayerUtils;
    private MediaPlayer mPlayer;// 播放器
    private int mDuration;
    private Timer mTimer;
    private OnMediaPlayerUtilsInterfa mOnMediaPlayerUtilsInterfa;

    private MediaPlayerUtils() {

    }

    public static MediaPlayerUtils getMediaPlayerUtils() {
        return sMediaPlayerUtils = sMediaPlayerUtils == null ? new MediaPlayerUtils() : sMediaPlayerUtils;
    }

    public MediaPlayerUtils init(Activity activity, String filePath) {
        if (null == mPlayer) {
            mPlayer = MediaPlayer.create(activity,
                    Uri.parse(filePath));
            if (null != mPlayer) {
                mPlayer.setOnCompletionListener(this);
                jsLengthSecond();
            } else {
                Log.e("MediaPlayer==null", "MediaPlayer播放对象初始化失败");
                empty();
                if (null != mOnMediaPlayerUtilsInterfa)
                    mOnMediaPlayerUtilsInterfa.onMediaPlayerError();
            }
        }
        return sMediaPlayerUtils;
    }

    private void jsLengthSecond() {
        int duration = mPlayer.getDuration();
        //求出音频长度的秒值
        mDuration = duration > -1 ? duration / 1000 : 0;
    }

    public int getDuration() {
        return mDuration;
    }

    public void start() {
        if (null != mPlayer) {
            mPlayer.start();
            recordTime();
        }
    }

    public void pause() {
        if (null != mPlayer) {
            mPlayer.pause();
            clearTime();
        }
    }

    public void empty() {
        if (null != mPlayer) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        clearTime();
    }

    private void clearTime() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 播放倒计时
     */
    private void recordTime() {
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                mDuration--;
                if (mDuration >= 0) {
                    if (null != mOnMediaPlayerUtilsInterfa)
                        mOnMediaPlayerUtilsInterfa.onMediaPlayerTime(mDuration);
                } else {
                    if (null != mPlayer) {
                        clearTime();
                        //停止上一次音频播放
                        mPlayer.stop();
                        try {
                            //准备下一次音频播放
                            mPlayer.prepare();
                        } catch (IOException e) {
                            empty();
                            if (null != mOnMediaPlayerUtilsInterfa)
                                mOnMediaPlayerUtilsInterfa.onMediaPlayerError();
                            e.printStackTrace();
                        }
                        jsLengthSecond();
                        if (null != mOnMediaPlayerUtilsInterfa)
                            mOnMediaPlayerUtilsInterfa.onMediaPlayerOk();
                    }
                }
            }

        };
        mTimer = new Timer();
        mTimer.schedule(timerTask, 1000, 1000);
    }

    /**
     * 根据秒数转换格式（格式：00:00）
     *
     * @param duration 秒值
     * @return
     */
    public String jsSecondMinuteText(OnTimeFormatTransition onTimeFormatTransition, int duration) {
        if (null != onTimeFormatTransition)
            return onTimeFormatTransition.onTimeFormatTransition(duration);
        return "秒数格式转换算法接口不能为空";
    }


    //播放完成监听
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    public MediaPlayerUtils setOnMediaPlayerUtilsInterfa(OnMediaPlayerUtilsInterfa onMediaPlayerUtilsInterfa) {
        mOnMediaPlayerUtilsInterfa = onMediaPlayerUtilsInterfa;
        return sMediaPlayerUtils;
    }

    public interface OnMediaPlayerUtilsInterfa {
        //播放倒计时回调
        public void onMediaPlayerTime(int time);

        //播放完毕回调
        public void onMediaPlayerOk();

        //播放错误回调，回调里面建议干掉当前界面，防止其它异常并发
        public void onMediaPlayerError();
    }
}
