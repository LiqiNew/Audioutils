package demo.liqi.audioutils.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import demo.liqi.audioutils.utils.model.AudioChannel;
import demo.liqi.audioutils.utils.model.AudioSampleRate;
import demo.liqi.audioutils.utils.model.AudioSource;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

/**
 * 录音辅助工具
 * Created by LiQi on 2016/12/20.
 */
public class MediaRecorderUtils implements PullTransport.OnAudioChunkPulledListener {
    //音频录制需要的参数配置
    private AudioSource source = AudioSource.MIC;
    private AudioChannel channel = AudioChannel.STEREO;
    private AudioSampleRate sampleRate = AudioSampleRate.HZ_48000;
    /**
     * 录音文件夹存储路径
     */
    private String mPath = Environment.getExternalStorageDirectory() + "LiQi/Recorder";
    /**
     * 录音文件名
     */
    private String mFileName = "LiQi.wav";
    /**
     * 秒钟
     */
    private int mSecond;
    /**
     * 分钟
     */
    private int mMinute;
    /**
     * 录音计时器
     */
    private Timer mTimer;
    /**
     * 录音文件最短事件限制
     */
    private long mLimitTime;
    /**
     * 录制音频最长限制（默认是2分钟）
     * 单位分钟
     */
    private int mTimeLength = 2;
    private Context mContext;
    private Recorder mRecorder;
    private OnMediaRecorderDisposeInterfa mOnMediaRecorderDisposeInterfa;

    private MediaRecorderUtils() {
    }

    MediaRecorderUtils(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 判断路径是否存在
     *
     * @param file
     * @return
     */
    private static boolean isFilePath(File file) {
        boolean isFile = false;
        if (file.exists() && file.isFile()) {
            isFile = true;
        } else {
            isFile = false;
        }
        return isFile;

    }

    /**
     * 初始化数据
     *
     * @param filePath 文件存储路径
     * @param fileName 文件存储名字
     * @return
     */
    public MediaRecorderUtils init(String filePath, String fileName) {
        if (null != filePath && !"".equals(filePath))
            mPath = filePath;

        //创建文件目录
        File file = new File(mPath);
        if (!isFilePath(file))
            file.mkdirs();

        if (null != fileName && !"".equals(fileName))
            mFileName = fileName + ".wav";
        mPath = mPath + "/" + mFileName;
        return this;
    }

    /**
     * 录音配置参数设置（要传的参数允许为null）
     *
     * @param source
     * @param channel
     * @param sampleRate
     * @return
     */
    public MediaRecorderUtils parameterSet(AudioSource source, AudioChannel channel, AudioSampleRate sampleRate) {
        if (null != source)
            this.source = source;
        if (null != channel)
            this.channel = channel;
        if (null != sampleRate)
            this.sampleRate = sampleRate;
        return this;
    }

    /**
     * 创建一个录音对象
     */
    private void newMediaRecorder() {
        if (null == mRecorder)
            mRecorder = OmRecorder.wav(
                    new PullTransport.Default(Util.getMic(source, channel, sampleRate), this),
                    new File(mPath));
    }

    public MediaRecorderUtils setTimeLength(int timeLength) {
        mTimeLength = timeLength;
        return this;
    }

    /**
     * 开始录音
     */
    public void startRecord() {

        if (getTimeDiffer()) {
            newMediaRecorder();
            if (mRecorder != null) {
                mRecorder.resumeRecording();
                mLimitTime = System.currentTimeMillis();
                recordTime();
            }
        }
    }

    /**
     * 暂停录音
     */
    public boolean pauseRecord() {
        if (!getTimeDiffer()) {
            //录音文件不得低于3秒钟
            Toast.makeText(mContext, "录音时间长度不得低于3秒钟！", Toast.LENGTH_SHORT).show();
            return false;
        }
        pauseRelease();
        return true;
    }

    /**
     * 录音对象清除
     */
    public void release() {
        clearRecorder();
        timeInit();
    }

    private void pauseRelease() {
        if (null != mRecorder)
            mRecorder.pauseRecording();
        clearTime();
    }

    /**
     * 清空mMediaRecorder
     */
    private void clearRecorder() {
        if (null != mRecorder) {
            mRecorder.stopRecording();
            mRecorder = null;
        }
    }

    /**
     * 录音结束 、时间归零,时间滚动线程取消
     */
    private void timeInit() {
        clearTime();
        mSecond = 0;
        mMinute = 0;
        mLimitTime = 0;
    }

    /**
     * 停止mTimer
     */
    private void clearTime() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 保存录音
     */
    public void saveRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pauseRelease();
                timeInit();
                File file = new File(mPath);
                if (isFilePath(file)) {
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderOK(file);
                }
            }
        }).start();
    }

    /**
     * 重新录音
     */
    public void cleartDataState() {
        timeInit();
        clearRecorder();
        //用户初始化录音功能时，清除上一个已经录音的文件
        File file = new File(mPath);
        if (isFilePath(file))
            file.delete();
    }


    /**
     * 录音计时
     */
    private void recordTime() {
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                mSecond++;
                if (mSecond >= 60) {
                    mSecond = 0;
                    mMinute++;
                }
                if (mMinute >= mTimeLength) {
                    timeInit();
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderLengthTime(true);
                    return;
                } else {
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderTime(String.format("%1$02d:%2$02d", mMinute, mSecond));
                }
            }

        };
        mTimer = new Timer();
        mTimer.schedule(timerTask, 1000, 1000);
    }

    /**
     * 当前时间点击是否大于3秒
     *
     * @return
     */
    private boolean getTimeDiffer() {
        if (System.currentTimeMillis() - mLimitTime > 3000) {
            return true;
        }
        return false;
    }

    /**
     * 把时间切割成指定格式的数据
     *
     * @param time 指定格式的数据。格式为00:00
     * @return
     */
    public String timeVoice(String time) {
        String date = "0s";
        if (null != time && !"".equals(time)) {
            String[] split = time.split(":");
            if (split.length > 0) {
                String spMinute = split[0];
                int minute = Integer.parseInt(spMinute);
                if (minute < 10) {
                    spMinute = jsTimeSecond(minute);
                }
                String spSecond = split[1];
                int second = Integer.parseInt(split[1]);
                if (second < 10) {
                    spSecond = jsTimeSecond(second);
                }
                if (spMinute != null && spSecond != null) {
                    date = spMinute + "'" + spSecond + "\"";
                } else if (spMinute == null && spSecond != null) {
                    date = spSecond + "s";
                } else if (spMinute != null && spSecond == null) {
                    date = spMinute + "'";
                }
            }
        }
        return date;
    }

    @Nullable
    private String jsTimeSecond(int spMinute) {
        String sp = null;
        if (spMinute > 0) {
            sp = spMinute + "";
        } else {
            sp = null;
        }
        return sp;
    }

    public MediaRecorderUtils setOnMediaRecorderDisposeInterfa(OnMediaRecorderDisposeInterfa onMediaRecorderDisposeInterfa) {
        mOnMediaRecorderDisposeInterfa = onMediaRecorderDisposeInterfa;
        return this;
    }

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {

    }

    public interface OnMediaRecorderDisposeInterfa {
        //录制时间回调，子线程
        public void onMediaRecorderTime(String time);

        //录制的时间是否超过最大长度限制回调，子线程
        public void onMediaRecorderLengthTime(boolean lengthTag);

        //录制错误回调
        public void onMediaRecorderError();

        //录制成功回调,子线程
        public void onMediaRecorderOK(File pathFile);
    }
}
