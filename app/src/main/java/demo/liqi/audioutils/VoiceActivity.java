package demo.liqi.audioutils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import demo.liqi.audioutils.utils.MediaPlayerUtils;
import demo.liqi.audioutils.utils.MediaRecorderUtils;
import demo.liqi.audioutils.utils.StateAudioObjUtils;
import demo.liqi.audioutils.utils.algorithm.SecondsTimeFormatText;
import demo.liqi.audioutils.utils.algorithm.SecondsTimeFormatTime;

import java.io.File;


/**
 * 声音录制和播放界面
 * <p/>
 * 由于我是把两个界面合在一个activity里面，所以此对象的逻辑有点复杂。
 * 如果你的业务逻辑不是这个样子，请你只看调用音频录制和播放的方法
 * Created by LiQi on 2016/12/19.
 */
public class VoiceActivity extends AppCompatActivity implements View.OnClickListener, MediaRecorderUtils.OnMediaRecorderDisposeInterfa,
        MediaPlayerUtils.OnMediaPlayerUtilsInterfa {
    //录音标识
    public final static int RECORD_TAG = 0x1;
    //播放录音标识
    public final static int PLAY_RECORD_TAG = 0x2;
    //语音录制存储文件夹
    private final static String FILEVOICE = "Liqi/voice";

    private final String SAVE = "保存", OK = "确定";
    //录音时间message标识
    private final int VOICE_RECORD_TIME = 0x12;
    //录音完成message标识
    private final int VOICE_RECORD_OK = 0X13;
    //录音错误message标识
    private final int ERROR_TAG = 0x14;
    //录音超过限制message标识
    private final int LENGTHTIME = 0x15;
    //播音倒计时message标识
    private final int MEDIAPLAYERTAG = 0x16;
    //播音完毕message标识
    private final int MEDIAPLAYEROK = 0X17;
    //录音文件名字
    private final String FILENAME = "YuHui";
    private Toolbar mVoiceToolbar;
    /**
     * 重录，保存|确定
     */
    private TextView mVoiceRerecording, mVoiceOk;

    //录音----------------------
    /**
     * 录音（点击录音|停止录音），播放录音（播放录音|暂停录音）
     */
    private ImageView mVoicePlay;
    /**
     * 录音提示（点击录音|停止录音），播放录音（不显示）
     */
    private TextView mVoiceGoHint;
    /**
     * 录音（初始化提示）
     */
    private TextView mVoiceHintLayout;

    /**
     * 录音（时间显示布局）
     */
    private LinearLayout mVoiceRecordLayout;
    /**
     * 录音（音频录取时间）
     */
    private TextView mVoiceRecordTime;
    /**
     * 音频播放布局
     */
    private RelativeLayout mVoicePlayLayout;
    /**
     * 音频播放时间
     */
    private TextView mVoicePlayTime;
    /**
     * 判断当前页面显示那种状态的布局
     */
    private int layoutTag = -1;
    /**
     * 录音辅助工具对象
     */
    private MediaRecorderUtils mMediaRecorderUtils;
    private MediaPlayerUtils mMediaPlayerUtils;
    private boolean playTag = false;
    private ProgressDialog mDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VOICE_RECORD_TIME:
                    String time = (String) msg.obj;
                    mVoiceRecordTime.setText(null == time ? "00:00" : time);
                    break;
                case VOICE_RECORD_OK:
                    closeDialog();
                    Toast.makeText(VoiceActivity.this, "录音保存成功", Toast.LENGTH_SHORT).show();
                    File file = (File) msg.obj;
                    try {
                        Thread.sleep(500);
                        Intent intent = getIntent();
                        intent.putExtra("file", file);
                        intent.putExtra("time", mMediaRecorderUtils.timeVoice(mVoiceRecordTime.getText().toString().trim()));
                        setResult(VoiceActivity.PLAY_RECORD_TAG, intent);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case ERROR_TAG:
                    Toast.makeText(VoiceActivity.this, msg.obj + "", Toast.LENGTH_SHORT).show();
                    closeDialog();
                    try {
                        Thread.sleep(500);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case LENGTHTIME:
                    showDialog("录音保存中...").show();
                    mVoiceRecordTime.setText("02:00");
                    mMediaRecorderUtils.saveRecord();
                    break;
                case MEDIAPLAYERTAG:
                    int mediaplayerTime = (int) msg.obj;
                    if (mediaplayerTime >= 0) {
                        if (null != mMediaPlayerUtils) {
                            String minuteText = mMediaPlayerUtils.jsSecondMinuteText(new SecondsTimeFormatText(), mediaplayerTime);
                            String minuteTime = mMediaPlayerUtils.jsSecondMinuteText(new SecondsTimeFormatTime(), mediaplayerTime);
                            mVoicePlayTime.setText("时间转换格式一：" + minuteText + "\n" + "时间转换格式二：" + minuteTime);
                        }
                    }
                    break;
                case MEDIAPLAYEROK:
                    mediaPlayerPauseState();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_activity);
        mVoiceToolbar = (Toolbar) findViewById(R.id.voice_toolbar);
        setSupportActionBar(mVoiceToolbar);
        init();
    }

    private void init() {
        //设置Toolbar左边无标题
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //设置左边图片
        mVoiceToolbar.setNavigationIcon(R.drawable.ico_back_before);
        //左边图片点击监听
        mVoiceToolbar.setNavigationOnClickListener(this);
        mVoiceRerecording = (TextView) findViewById(R.id.voice_rerecording);
        mVoiceOk = (TextView) findViewById(R.id.voice_ok);
        mVoiceRerecording = (TextView) findViewById(R.id.voice_rerecording);
        mVoiceRerecording.setOnClickListener(this);
        mVoiceOk = (TextView) findViewById(R.id.voice_ok);
        mVoiceOk.setOnClickListener(this);
        mVoicePlay = (ImageView) findViewById(R.id.voice_play);
        mVoicePlay.setOnClickListener(this);
        mVoiceGoHint = (TextView) findViewById(R.id.voice_go_hint);
        mVoiceHintLayout = (TextView) findViewById(R.id.voice_hint_layout);
        mVoiceRecordLayout = (LinearLayout) findViewById(R.id.voice_record_layout);
        mVoiceRecordTime = (TextView) findViewById(R.id.voice_record_time);
        mVoicePlayLayout = (RelativeLayout) findViewById(R.id.voice_play_layout);
        mVoicePlayTime = (TextView) findViewById(R.id.voice_play_time);
        String layoutTagString = getIntent().getStringExtra("layoutTag");
        layoutTag = Integer.parseInt(null != layoutTagString && !"".equals(layoutTagString) ? layoutTagString : "-1");
        switch (layoutTag) {
            //录音
            case RECORD_TAG:
                iniRecordLayou();
                break;
            //播放录音
            case PLAY_RECORD_TAG:
                iniPlayLayou();
                break;
        }
    }

    /**
     * 初始化录音布局
     */
    private void iniRecordLayou() {
        setTextState(mVoiceRerecording, R.color.hint_grey, false, "重录");
        setTextState(mVoiceOk, R.color.hint_grey, false, SAVE);
        mVoicePlay.setImageResource(R.drawable.con_voice_record);
        widgetVisible(mVoiceGoHint);
        mVoiceGoHint.setText("点击录音");
        widgetVisible(mVoiceHintLayout);
        widgetGone(mVoiceRecordLayout);
        widgetGone(mVoicePlayLayout);
        if (null == mMediaRecorderUtils)
            mMediaRecorderUtils = StateAudioObjUtils.getRecorderUtils(this).init(getPath(this, FILEVOICE), FILENAME).setOnMediaRecorderDisposeInterfa(this);
    }

    /**
     * 初始化播音布局
     */
    private void iniPlayLayou() {
        setTextState(mVoiceRerecording, R.color.textColorPrimary, true, "重录");
        setTextState(mVoiceOk, R.color.textColorPrimary, true, OK);
        mVoicePlay.setImageResource(R.drawable.con_voice_pause);
        widgetVisible(mVoicePlayLayout);
        widgetGone(mVoiceHintLayout);
        widgetGone(mVoiceRecordLayout);
        widgetGone(mVoiceGoHint);
        File file = (File) getIntent().getSerializableExtra("obj");
        String path = "";
        if (null != file)
            path = file.getPath();
        Log.e("声音播放路径", path);
        mMediaPlayerUtils = MediaPlayerUtils.getMediaPlayerUtils().init(this, path).setOnMediaPlayerUtilsInterfa(this);
        String minuteText = mMediaPlayerUtils.jsSecondMinuteText(new SecondsTimeFormatText(), mMediaPlayerUtils.getDuration());
        String minuteTime = mMediaPlayerUtils.jsSecondMinuteText(new SecondsTimeFormatTime(), mMediaPlayerUtils.getDuration());
        mVoicePlayTime.setText("时间转换格式一：" + minuteText + "\n" + "时间转换格式二：" + minuteTime);
    }

    /**
     * 控件显示
     *
     * @param view
     */
    private void widgetVisible(View view) {
        if (view.getVisibility() == View.GONE)
            view.setVisibility(View.VISIBLE);
    }

    /**
     * 控件隐藏
     *
     * @param view
     */
    private void widgetGone(View view) {
        if (view.getVisibility() == View.VISIBLE)
            view.setVisibility(View.GONE);
    }

    private void setTextState(TextView textView, int colorId, boolean enabled, String content) {
        textView.setTextColor(ContextCompat.getColor(this, colorId));
        textView.setEnabled(enabled);
        textView.setText(content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //返回
            case -1:
                if (layoutTag == RECORD_TAG)
                    setResult(RECORD_TAG);
                finish();
                break;
            //重录
            case R.id.voice_rerecording:
                switch (layoutTag) {
                    //录音
                    case RECORD_TAG:
                        iniRecordLayou();
                        break;
                    //播放录音
                    case PLAY_RECORD_TAG:
                        layoutTag = RECORD_TAG;
                        if (null != mMediaPlayerUtils) {
                            mMediaPlayerUtils.empty();
                            mMediaPlayerUtils = null;
                        }
                        iniRecordLayou();
                        break;
                }
                playTag = false;
                mMediaRecorderUtils.cleartDataState();
                break;
            //保存|确定
            case R.id.voice_ok:
                String okAndSave = mVoiceOk.getText().toString().trim();
                //保存
                if (SAVE.equals(okAndSave)) {
                    showDialog("录音保存中...").show();
                    mMediaRecorderUtils.saveRecord();
                }
                //确定
                else if (OK.equals(okAndSave)) {
                    finish();
                } else {
                    return;
                }
                break;
            //（录音|停止），（播放|暂停）
            case R.id.voice_play:
                switch (layoutTag) {
                    //录音
                    case RECORD_TAG:
                        boolean pause = true;
                        //开启录音
                        if (!playTag) {
                            mMediaRecorderUtils.startRecord();
                        }
                        //暂停录音
                        else {
                            pause = mMediaRecorderUtils.pauseRecord();
                        }
                        if (pause) {
                            widgetGone(mVoiceHintLayout);
                            widgetVisible(mVoiceRecordLayout);
                            setTextState(mVoiceRerecording, R.color.textColorPrimary, true, "重录");
                            setTextState(mVoiceOk, R.color.textColorPrimary, true, SAVE);
                            setImageStateDrawable(R.drawable.con_voice_recording, R.drawable.con_voice_record);
                            setTextViewStateHint();
                            playTag = !playTag;
                        }
                        break;
                    //播放录音
                    case PLAY_RECORD_TAG:
                        setImageStateDrawable(R.drawable.con_voice_play, R.drawable.con_voice_pause);
                        if (!playTag)
                            mMediaPlayerUtils.start();
                        else
                            mMediaPlayerUtils.pause();
                        playTag = !playTag;
                        break;
                }
                break;
        }
    }

    /**
     * 设置图片点击切换图片和图片底部提示语
     *
     * @param toDrawable 正在录音或者正在播放音频的图片ID
     * @param inDrawable 还未录音或者还未播放音频的图片ID
     */
    private void setImageStateDrawable(int toDrawable, int inDrawable) {
        if (!playTag)
            mVoicePlay.setImageResource(toDrawable);
        else
            mVoicePlay.setImageResource(inDrawable);
    }

    private void setTextViewStateHint() {
        if (!playTag)
            mVoiceGoHint.setText("暂停录音");
        else
            mVoiceGoHint.setText("继续录音");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (layoutTag == RECORD_TAG)
                setResult(RECORD_TAG);
            finish();
        }
        return true;
    }

    private Message getMessage(int tag, Object object) {
        Message message = mHandler.obtainMessage();
        message.what = tag;
        message.obj = object;
        return message;
    }

    @Override
    public void onMediaRecorderTime(String time) {
        mHandler.sendMessage(getMessage(VOICE_RECORD_TIME, time));
    }

    @Override
    public void onMediaRecorderLengthTime(boolean lengthTag) {
        mHandler.sendMessage(getMessage(LENGTHTIME, lengthTag));
    }

    @Override
    public void onMediaRecorderError() {
        mHandler.sendMessage(getMessage(ERROR_TAG, "录音初始化出错，请重试"));
    }

    @Override
    public void onMediaRecorderOK(File pathFile) {
        mHandler.sendMessage(getMessage(VOICE_RECORD_OK, pathFile));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mMediaRecorderUtils)
            mMediaRecorderUtils.release();
        if (null != mMediaPlayerUtils)
            mMediaPlayerUtils.empty();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMediaRecorderUtils && layoutTag == RECORD_TAG) {
            boolean pauseRecord = mMediaRecorderUtils.pauseRecord();
            if (pauseRecord)
                pauseState();
        }
        if (null != mMediaPlayerUtils && layoutTag == PLAY_RECORD_TAG) {
            mMediaPlayerUtils.pause();
            mediaPlayerPauseState();
        }
    }

    /**
     * 录音暂停状态
     */
    private void pauseState() {
        playTag = true;
        setImageStateDrawable(R.drawable.con_voice_recording, R.drawable.con_voice_record);
        setTextViewStateHint();
        playTag = false;
    }

    @Override
    public void onMediaPlayerTime(int time) {
        mHandler.sendMessage(getMessage(MEDIAPLAYERTAG, time));
    }

    @Override
    public void onMediaPlayerOk() {
        mHandler.sendMessage(getMessage(MEDIAPLAYEROK, null));
    }

    private void mediaPlayerPauseState() {
        playTag = true;
        setImageStateDrawable(R.drawable.con_voice_play, R.drawable.con_voice_pause);
        playTag = false;
    }

    @Override
    public void onMediaPlayerError() {
        mHandler.sendMessage(getMessage(ERROR_TAG, "播音初始化出错,请重试"));
    }

    /**
     * 获取保存路径
     *
     * @param activity
     * @param name     路径名字
     * @return
     */
    private String getPath(Context activity, String name) {
        String path = "";
        // 判断是否安装有SD卡
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory() + "/" + name;
        } else {
            path = activity.getCacheDir() + "/" + name;
        }
        return path;
    }

    /**
     * 获取进度条框
     *
     * @param content
     * @return
     */
    protected ProgressDialog showDialog(String content) {
        if (null == mDialog) {
            mDialog = new ProgressDialog(this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mDialog.setMessage(content);
        return mDialog;
    }

    /**
     * 手动关闭进度条加载框
     */
    protected void closeDialog() {
        if (null != mDialog && mDialog.isShowing())
            mDialog.dismiss();
    }
}
