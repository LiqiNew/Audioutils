package demo.liqi.audioutils.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 录音辅助工具
 * Created by LiQi on 2016/12/20.
 */
public class MediaRecorderUtilsAmr {
    /**
     * 待合成的录音片段
     */
    private final ArrayList<String> mList = new ArrayList<String>();
    /**
     * 录音文件夹存储路径
     */
    private String mPath = "LiQi/Recorder";
    /**
     * 录音文件名
     */
    private String mFileName = "LiQi.amr";
    /**
     * 临时录音路径
     */
    private String mTemporarily;
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
     * 录制音频最长限制（默认是10分钟）
     * 单位分钟
     */
    private int mTimeLength = 10;
    private Context mContext;
    private MediaRecorder mMediaRecorder;
    private OnMediaRecorderDisposeInterfa mOnMediaRecorderDisposeInterfa;
    //判断是否是暂停
    private boolean isPause = false;

    MediaRecorderUtilsAmr(Context mContext) {
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
    public MediaRecorderUtilsAmr init(String filePath, String fileName) {
        mFileName = fileName;
        mPath = null != filePath && !"".equals(filePath) ? filePath : mPath;
        File file = new File(mPath);
        if (!isFilePath(file))
            file.mkdirs();
        return this;
    }

    /**
     * 创建一个录音对象
     */
    private void newMediaRecorder() {
        pauseRelease();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 选择amr格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mTemporarily);
    }

    public MediaRecorderUtilsAmr setTimeLength(int timeLength) {
        mTimeLength = timeLength;
        return this;
    }

    /**
     * 开始录音
     */
    public void startRecord() {

        if (getTimeDiffer()) {
            mTemporarily = mPath + "/" + System.currentTimeMillis() + ".amr";
            newMediaRecorder();
            try {
                mMediaRecorder.prepare();
            } catch (Exception e) {
                release();
                if (null != mOnMediaRecorderDisposeInterfa)
                    mOnMediaRecorderDisposeInterfa.onMediaRecorderError();
            }
            if (mMediaRecorder != null) {
                try {
                    mMediaRecorder.start();
                } catch (IllegalStateException e) {
                    release();
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderError();
                }
                mLimitTime = System.currentTimeMillis();
                recordTime();
            }
            isPause = false;
        }
    }

    /**
     * 暂停录音
     */
    public boolean pauseRecord() {
        if (!getTimeDiffer()) {
            //录音文件不得低于3秒钟
            Toast.makeText(mContext,"录音时间长度不得低于3秒钟！",Toast.LENGTH_SHORT).show();
            return false;
        }
        pauseRelease();
        if (null != mTemporarily && !"".equals(mTemporarily))
            // 将录音片段加入列表
            mList.add(mTemporarily);
        isPause = true;
        return true;
    }

    /**
     * 录音对象清除
     */
    public void release() {
        clearRecorder();
        timeInit();
        deleteTemporarilyFile();
    }

    private void pauseRelease() {
        clearRecorder();
        clearTime();
    }

    /**
     * 清空mMediaRecorder
     */
    private void clearRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
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
                // 将正在录音片段加入列表
                if (!isPause)
                    mList.add(mTemporarily);
                // 最后合成的音频文件路径
                String fileName = mPath + "/" + mFileName + ".amr";
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(fileName);
                } catch (FileNotFoundException e) {
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderError();
                    e.printStackTrace();
                }
                FileInputStream fileInputStream = null;
                try {
                    for (int i = 0; i < mList.size(); i++) {
                        File file = new File(mList.get(i));
                        // 把因为暂停所录出的多段录音进行读取
                        fileInputStream = new FileInputStream(file);
                        byte[] mByte = new byte[fileInputStream.available()];
                        int length = mByte.length;
                        // 第一个录音文件的前六位是不需要删除的
                        if (i == 0) {
                            while (fileInputStream.read(mByte) != -1) {
                                fileOutputStream.write(mByte, 0, length);
                            }
                        }
                        // 之后的文件，去掉前六位
                        else {
                            while (fileInputStream.read(mByte) != -1) {

                                fileOutputStream.write(mByte, 6, length - 6);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 这里捕获流的IO异常，万一系统错误需要提示用户
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderError();
                    e.printStackTrace();
                } finally {
                    try {
                        fileOutputStream.flush();
                        fileInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                timeInit();
                deleteTemporarilyFile();
                File file = new File(fileName);
                if (isFilePath(file)) {
                    if (null != mOnMediaRecorderDisposeInterfa)
                        mOnMediaRecorderDisposeInterfa.onMediaRecorderOK(file);
                }
            }
        }).start();
    }

    /**
     * 清空当前数据状态
     */
    public void cleartDataState() {
        isPause = false;
        timeInit();
        deleteTemporarilyFile();
        //用户初始化录音功能时，清除上一个已经录音的文件
        File file = new File(mPath + "/" + mFileName + ".amr");
        if (isFilePath(file))
            file.delete();
    }


    /**
     * 删除临时录音片段文件
     */
    private void deleteTemporarilyFile() {
        // 将正在录音片段加入列表
        if (!isPause && null != mTemporarily)
            mList.add(mTemporarily);
        if (!mList.isEmpty()) {
            // 不管合成是否成功、删除录音片段
            for (int i = 0; i < mList.size(); i++) {
                File file = new File(mList.get(i));
                if (isFilePath(file)) {
                    file.delete();
                }
            }
            mList.clear();
        }
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

    public MediaRecorderUtilsAmr setOnMediaRecorderDisposeInterfa(OnMediaRecorderDisposeInterfa onMediaRecorderDisposeInterfa) {
        mOnMediaRecorderDisposeInterfa = onMediaRecorderDisposeInterfa;
        return this;
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
